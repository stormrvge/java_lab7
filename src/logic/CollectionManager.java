package logic;

import commands.OutOfBoundsException;
import commands.PermissionDeniedException;
import server.Server;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * This class realizing methods for commands.
 */
public class CollectionManager implements Serializable {
    private ArrayList<Route> route;
    private java.time.ZonedDateTime date;
    private final ReentrantLock lock;

    public CollectionManager() {
        date = java.time.ZonedDateTime.now();
        route = new ArrayList<>();
        lock = new ReentrantLock();
    }

    public void load(ResultSet res) throws SQLException {
        lock.lock();
        route.clear();
        while (res.next()) {
            route.add(Route.generateFromSQL(res));
        }
        lock.unlock();
    }

    /**
     * Method "info" which displays short instruction of every command program.
     */
    public String helpClient() {
        return ("info: вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)" +
                "\nshow: вывести в стандартный поток вывода все элементы коллекции в строковом представлении" +
                "\nadd {element}: добавить новый элемент в коллекцию" +
                "\nupdate_id {element}: обновить значение элемента коллекции, id которого равен заданному" +
                "\nremove_by_id id: удалить элемент из коллекции по его id" +
                "\nclear: очистить коллекцию" +
                "\nexecute_script file_name: считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме. + " +
                "\nexit: завершить программу (без сохранения в файл)" +
                "\nremove_at index: удалить элемент, находящийся в заданной позиции коллекции (index)" +
                "\nadd_if_max {element}: добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции" +
                "\nadd_if_min {element}: добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции" +
                "\ncount_by_distance distance: вывести количество элементов, значение поля distance которых равно заданному" +
                "\nprint_unique_distance distance: вывести уникальные значения поля distance" +
                "\nprint_field_ascending_distance distance: вывести значение поля distance в порядке возрастания");
    }

    /**
     * Method "info" which displays short instruction of every command program.
     */
    public void helpServer() {
        System.out.println("exit: закрыть сервер");
    }


    /**
     * This method print info about collection.
     */
    public String info() {
        lock.lock();
        try {
            Field arrayListField = CollectionManager.class.getDeclaredField("route");
            String arrayListType = arrayListField.getGenericType().getTypeName();
            String[] className = arrayListType.replace("<", " ").
                    replace(">", " ").split("[ .]");
            lock.unlock();
            return ("Type: "  + className[4] +   // className[5]
                    ", initializing date: " + date +
                    ", collection size: " + route.size());
        } catch (NoSuchFieldException e) {
            lock.unlock();
            return ("Problem with general class. Cant find type of class!");
        }
    }

    /**
     * This method shows a elements in collection.
     */
    public String show() {
        lock.lock();
        if (route.isEmpty()) return ("Collection is empty.");
        else {
            String str = route.stream()
                    .map(Route::toString)
                    .collect(Collectors.joining(("\n")));
            lock.unlock();
            return str;
        }
    }

    /**
     * This method add's a new element to collection.
     * bounds for coordinates and location class.
     */
    public String add(Server server, Route object, User user) {
        try {
            lock.lock();
            route.add(object);

            server.getSqlStatements().save(object, user);
            object.setId(server.getSqlStatements().getId());
            lock.unlock();
            return "Element was added";
        } catch (SQLException e) {
            lock.unlock();
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            return e.getMessage();
        }
        lock.unlock();
        return "Element wasn't added";
    }

    /**
     * This method update's an element in collection by id.
     * @param id - id of element which we want to update.
     */
    public String update_id(Integer id, Route newElement, Server server, User user) {
        lock.lock();
        try {
            Route oldElement = route.get(getIndexById(id));
            if (newElement != null) {
                oldElement.setName(newElement.getName());
                oldElement.setCoordinates(newElement.getCoordinates());
                oldElement.setFrom(newElement.getFrom());
                oldElement.setTo(newElement.getTo());
                oldElement.setDistance(newElement.getDistance());

                server.getSqlStatements().updateId(id, newElement, user);

                lock.unlock();
                return ("Element with " + id + " was updated!");
            }
        } catch (Exception e) {
            lock.unlock();
            return ("No element with such id!");
        }
        lock.unlock();
        return null;
        }

    /**
     * This method remove's element from collection by id.
     * @param id - argument from console.
     */
    public String remove_by_id(Server server, Integer id, User user) {
        lock.lock();
        try {
            removeByOwner(user.getUsername(), id);

            server.getSqlStatements().remove_route(id, user.getUsername());
            lock.unlock();
            return ("Element with " + id + " was removed!");
        } catch (OutOfBoundsException e) {
            lock.unlock();
            return ("No element with such id!");
        } catch (PermissionDeniedException e) {
            lock.unlock();
            return ("Permission denied.");
        } catch (SQLException e) {
            lock.unlock();
            return (e.getMessage());
        }
        /*
            route = route.stream()
                    .filter(x -> (x.getId() != id || !x.getOwner().equals(user.getUsername())))
                    .collect(Collectors.toCollection(ArrayList::new));  //NULL
             */
    }

    public String clear(Server server, User user) {             // FIX
        lock.lock();
        try {
            server.getSqlStatements().clearUserCollection(user.getUsername());

            ResultSet res = server.getDataBase().createStatement(ResultSet
                    .TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                    .executeQuery("SELECT * FROM collection");

            load(res);
            lock.unlock();
            return "Your collection was cleared.";
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            lock.unlock();
            return "Your collection wasn't cleared.";
        }
    }


    /**
     * This method delete element from collection by index.
     * @param index - argument from console.
     */
    public String remove_at(Server server, int index, User user) {
        lock.lock();
        try {
            int id = route.get(index).getId();
            removeByOwner(user.getUsername(), id);

            server.getSqlStatements().remove_route(id, user.getUsername());
            lock.unlock();
            return ("Element with " + id + " was removed!");
        } catch (PermissionDeniedException e) {
            lock.unlock();
            return "Permission denied";
        } catch (IndexOutOfBoundsException | OutOfBoundsException e) {
            lock.unlock();
            return "No element with such id";
        } catch (SQLException e) {
            lock.unlock();
            return e.getMessage();
        }
    }

    /**
     * This method will add new element, if distance of new element is maximal in collection.
     */
    public String add_if_max(Server server, Route object, User user) {
        lock.lock();
        try {
            if(route.size() > 0 && route.stream().max(Comparator.naturalOrder()).get().compareTo(object) > 0) {
                lock.unlock();
                return "That element isn't maximal in collection.";
            } else {
                server.getSqlStatements().save(object, user);
                object.setId(server.getSqlStatements().getId());
                route.add(object);
                lock.unlock();
                return "Element has been added successfully.";
            }
        } catch (SQLException e) {
            lock.unlock();
            System.err.println(e.getMessage());
        }
        lock.unlock();
        return "Element wasn't added";
    }


    public String add_if_min(Server server, Route object, User user) {
        lock.lock();
        try {
            if(route.size() > 0 && route.stream().min(Comparator.naturalOrder()).get().compareTo(object) < 0) {
                lock.unlock();
                return "That element isn't minimal in collection.";
            } else {
                server.getSqlStatements().save(object, user);
                object.setId(server.getSqlStatements().getId());
                route.add(object);
                lock.unlock();
                return "Element has been added successfully.";
            }
        } catch (SQLException e) {
            lock.unlock();
            System.err.println(e.getMessage());
        }
        lock.unlock();
        return "Element wasn't added";
    }


    public String count_by_distance(Float distance) {
        try {
            return ("Number of coincidences: " + route.stream()
                    .map(Route::getDistance)
                    .filter(dist -> dist.equals(distance))
                    .count());

        } catch (NumberFormatException e) {
            return ("Bad type of argument!");
        }
    }


    public String print_unique_distance() {
        lock.lock();
        HashSet<Float> floatHashSet = route.stream()
                .sorted(Route::compareTo)
                .map(Route::getDistance)
                .collect(Collectors.toCollection(HashSet::new));

        lock.unlock();
        return ("Unique distance: " + floatHashSet.toString());
    }

    /**
     * This method prints sorted collection in ascending by distance field.
     */
    public String print_field_ascending_distance() {
        lock.lock();
        ArrayList<Route> sortedRoute = route.stream()
                .sorted(Comparator.comparing(Route::getDistance))
                .collect(Collectors.toCollection(ArrayList::new));

        String str = "Sorted by distance: [";
        for (int i = 0; i < sortedRoute.size(); i++) {
            Route value = sortedRoute.get(i);
            str += value.getDistance();
            if (i + 1 < sortedRoute.size()) str += (", ");
        }

        lock.unlock();
        return str + "]";
    }


    /**
     * This method returning index of element in collection with id as parameter.
     * @param id - field of element.
     * @return - returns index.
     * @throws Exception - throws exception, if no elements with id from parameter.
     */
    private int getIndexById(int id) throws Exception {
        lock.lock();
        for (int i = 0; i < route.size(); i++) {
            if (route.get(i).getId() == id) {
                lock.unlock();
                return i;
            }
        }
        lock.unlock();
        throw new Exception("No such id");
    }

    private void removeByOwner(String owner, int id) throws PermissionDeniedException, OutOfBoundsException {
        boolean idIsFound = false;
        Iterator<Route> iterator = route.iterator();
        while (iterator.hasNext() && !idIsFound) {
            Route checkRoute = iterator.next();
            if (checkRoute.getId() == id && checkRoute.getOwner().equals(owner)) {
                iterator.remove();
                idIsFound = true;
            } else if (checkRoute.getId() == id && !checkRoute.getOwner().equals(owner)) {
                throw new PermissionDeniedException();
            } else if (!iterator.hasNext()) {
                throw new OutOfBoundsException();
            }
        }
    }

    private void setInitDate(java.time.ZonedDateTime date) {
        this.date = date;
    }
}