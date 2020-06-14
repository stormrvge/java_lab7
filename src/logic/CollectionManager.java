package logic;

import server.Server;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class realizing methods for commands.
 */
public class CollectionManager implements Serializable {
    private ArrayList<Route> route;
    private java.time.ZonedDateTime date;

    public CollectionManager() {
        date = java.time.ZonedDateTime.now();
        route = new ArrayList<>();
    }

    public void load(ResultSet res) throws SQLException {
        while (res.next()) {
            route.add(Route.generateFromSQL(res));
        }
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
        System.out.println("save: сохранить коллекцию в файл" +
                "\nexit: завершить программу (без сохранения в файл)");
    }


    /**
     * This method print info about collection.
     */
    public String info() {
        try {
            Field arrayListField = CollectionManager.class.getDeclaredField("route");
            String arrayListType = arrayListField.getGenericType().getTypeName();
            String[] className = arrayListType.replace("<", " ").
                    replace(">", " ").split("[ .]");
            return ("Type: "  + className[4] +   // className[5]
                    ", initializing date: " + date +
                    ", collection size: " + route.size());
        } catch (NoSuchFieldException e) {
            return ("Problem with general class. Cant find type of class!");
        }
    }

    /**
     * This method shows a elements in collection.
     */
    public String show() {
        if (route.isEmpty()) return ("Collection is empty.");
        else {
            String str = route.stream()
                    .map(Route::toString)
                    .collect(Collectors.joining(("\n")));
            return str;
        }
    }

    /**
     * This method add's a new element to collection.
     * bounds for coordinates and location class.
     */
    public String add(Server server, Route object, User user) {
        try {
            server.save(object, user);
            object.setId(server.getId());
            route.add(object);
            return "Element was added";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "Element wasn't added";
    }

    /**
     * This method update's an element in collection by id.
     * @param id - id of element which we want to update.
     */
    public String update_id(Integer id, Route newElement, Server server, User user) {
        try {
            Route oldElement = route.get(getIndexById(id));
            if (newElement != null) {
                oldElement.setName(newElement.getName());
                oldElement.setCoordinates(newElement.getCoordinates());
                oldElement.setFrom(newElement.getFrom());
                oldElement.setTo(newElement.getTo());
                oldElement.setDistance(newElement.getDistance());

                server.updateId(id, newElement, user);


                return ("Element with " + id + " was updated!");
            }
        } catch (Exception e) {
            return ("No element with such id!");
        }
        return null;
        }

    /**
     * This method remove's element from collection by id.
     * @param id - argument from console.
     */
    public String remove_by_id(Integer id) {
        try {
            route = route.stream()
                    .filter(Route -> Route.getId() != id)
                    .collect(Collectors.toCollection(ArrayList::new));
            return ("Element with " + id + " was removed!");
        } catch (Exception e) {
            return ("No element with such id!");
        }
    }

    /**
     * This method clear's collection (deleting all elements).
     */
    public String clear(Server server, User user) {
        try {
            server.clearUserCollection(user.getUsername());
            removeByOwner(user.getUsername());
            return "Your collection was cleared.";
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Your collection wasn't cleared.";
        }
    }


    /**
     * This method delete element from collection by index.
     * @param index - argument from console.
     */
    public String remove_at(Integer index) {
        try {
            int idx = index;
            route.remove(idx);
            return ("Element with index " + index + " was deleted.");
        } catch (Exception e) {
            return ("No element with such index!");
        }
    }

    /**
     * This method will add new element, if distance of new element is maximal in collection.
     */
    public String add_if_max(Server server, Route object, User user) {
        try {
            if(route.size() > 0 && route.stream().max(Comparator.naturalOrder()).get().compareTo(object) > 0) {
                return "That element isn't maximal in collection.";
            } else {
                server.save(object, user);
                object.setId(server.getId());
                route.add(object);
                return "Element has been added successfully.";
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return "Element wasn't added";
    }


    public String add_if_min(Server server, Route object, User user) {
        try {
            if(route.size() > 0 && route.stream().min(Comparator.naturalOrder()).get().compareTo(object) > 0) {
                return "That element isn't minimal in collection.";
            } else {
                server.save(object, user);
                object.setId(server.getId());
                route.add(object);
                return "Element has been added successfully.";
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
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
        HashSet<Float> floatHashSet = route.stream()
                .sorted(Route::compareTo)
                .map(Route::getDistance)
                .collect(Collectors.toCollection(HashSet::new));

        return ("Unique distance: " + floatHashSet.toString());
    }

    /**
     * This method prints sorted collection in ascending by distance field.
     */
    public String print_field_ascending_distance() {
        ArrayList<Route> sortedRoute = route.stream()
                .sorted(Comparator.comparing(Route::getDistance))
                .collect(Collectors.toCollection(ArrayList::new));

        String str = "Sorted by distance: [";
        for (int i = 0; i < sortedRoute.size(); i++) {
            Route value = sortedRoute.get(i);
            str += value.getDistance();
            if (i + 1 < sortedRoute.size()) str += (", ");
        }

        return str + "]";
    }


    /**
     * This method returning index of element in collection with id as parameter.
     * @param id - field of element.
     * @return - returns index.
     * @throws Exception - throws exception, if no elements with id from parameter.
     */
    private int getIndexById(int id) throws Exception {
        for (int i = 0; i < route.size(); i++) {
            if (route.get(i).getId() == id) {
                return i;
            }
        } throw new Exception("No such id");
    }

    private void removeByOwner(String owner) {
        route = route.stream()
                .filter(x -> x.getName().equals(owner))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void setInitDate(java.time.ZonedDateTime date) {
        this.date = date;
    }
}