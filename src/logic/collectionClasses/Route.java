package logic.collectionClasses;

import commands.exceptions.OutOfBoundsException;
import logic.User;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * The general class in our collection. Collection contains elements of routes.
 * Class route contains coordinate and location classes.
 */
public class Route implements Comparable<Route>, Serializable {
    private int id = 0; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private static int lastIdAdded = 1;
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private final java.time.ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Location from; //Поле не может быть null
    private Location to; //Поле может быть null
    private float distance; //Значение поля должно быть больше 1
    private String owner;

    /**
     * Default constructor for setters.
     */
    public Route() {
        to = new Location();
        from = new Location();
        coordinates = new Coordinates();
        creationDate = java.time.ZonedDateTime.now();
    }

    Route(String name, Coordinates coordinates, Location from, Location to, float distance, String owner)
            throws NullPointerException, OutOfBoundsException {
        if (distance < 1) throw new OutOfBoundsException();
        if (name == null || coordinates == null || from == null || to == null)
            throw new NullPointerException();

        this.id = lastIdAdded++;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = java.time.ZonedDateTime.now();
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.owner = owner;
    }

    public void setName(String name) {
        if (name == null || name.trim().equals("")) throw new NullPointerException("Name cant be null");
        this.name = name;
    }

    public void setCoordinates(double x, double y) {
        try {
            coordinates.setX(x);
            coordinates.setY(y);
        } catch (OutOfBoundsException e) {
            System.err.println(e.getMessage());
        }
    }

    public void setFrom(float x, int y, int z) {
        from.setX(x);
        from.setY(y);
        from.setZ(z);
    }

    public void setTo(float x, int y, int z) {
        to.setX(x);
        to.setY(y);
        to.setZ(z);
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setFrom(Location from) {
        this.from = from;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    public void setDistance(float distance) throws OutOfBoundsException {
        if (distance < 1) throw new OutOfBoundsException();
        this.distance = distance;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {return this.id;}

    public String getName() {return this.name;}

    public Coordinates getCoordinates() {return coordinates;}

    public Location getFrom() {return from;}

    public Location getTo() {return to;}

    public float getDistance() {return distance;}

    public String getOwner() {return owner;}

    public static Route generateObjectUserInput(User user) {
        try {
            Scanner input = new Scanner(System.in);

            System.out.println("Enter name: ");
            String name = input.nextLine();

            Location locationFrom = Location.generateObjectUserInput();
            Location locationTo = Location.generateObjectUserInput();
            //LOCATION TO CAN BE NULL!

            Coordinates coordinates = Coordinates.generateObjectUserInput();

            System.out.println("Enter distance (float): ");
            float distance = Float.parseFloat(input.nextLine());
            return new Route(name, coordinates, locationFrom, locationTo, distance, user.getUsername());
        } catch (OutOfBoundsException e) {
            System.out.println("Out of bounds exception");
        } catch (NumberFormatException e) {
            System.err.println("Incorrect format for that field.");
        } catch (NullPointerException e) {
            System.err.println("Element cant be null");
        }
        return null;
    }

    public static Route generateFromSQL(ResultSet res) {
        Route route = new Route();

        try {
            route.setId(res.getInt(1));
            route.setName(res.getString(2));
            route.setCoordinates(res.getDouble(3), res.getDouble(4));
            route.setFrom(res.getFloat(5), res.getInt(6), res.getInt(7));
            route.setTo(res.getFloat(8), res.getInt(9), res.getInt(10));
            route.setDistance(res.getFloat(11));
            route.setOwner(res.getString(12));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return route;
    }

    public void add(PreparedStatement st, User user) throws SQLException {
        st.setString(1, getName());
        st.setDouble(2, coordinates.getX());
        st.setDouble(3, coordinates.getY());
        st.setFloat(4, from.getX());
        st.setInt(5, from.getY());
        st.setInt(6, from.getZ());
        st.setFloat(7, to.getX());
        st.setInt(8, to.getY());
        st.setInt(9, to.getZ());
        st.setFloat(10, getDistance());
        st.setString(11, user.getUsername());

        st.executeUpdate();
    }

    public void update_id(PreparedStatement st, User user, int id) throws SQLException {
        st.setString(1, getName());
        st.setDouble(2, coordinates.getX());
        st.setDouble(3, coordinates.getY());
        st.setFloat(4, from.getX());
        st.setInt(5, from.getY());
        st.setInt(6, from.getZ());
        st.setFloat(7, to.getX());
        st.setInt(8, to.getY());
        st.setInt(9, to.getZ());
        st.setFloat(10, getDistance());
        st.setInt(11, id);
        st.setString(12, user.getUsername());

        st.executeUpdate();
    }

    @Override
    public int compareTo(Route route) {
        return Float.compare(distance, route.getDistance());
    }

    @Override
    public String toString() {
        if (to == null) {
            return ("Route [id = " + id + ", name = " + name + ", coordinates = " + coordinates.toString() +
                    ", creation date = " + creationDate.getYear() + "-"  + creationDate.getMonthValue() + "-" +
                    creationDate.getDayOfMonth() + ", location from = " + from.toString() + ", location to = null"
                    + ", distance = " + distance + "]");
        }
        else {
            return ("Route [id = " + id + ", name = " + name + ", coordinates = " + coordinates.toString() +
                    ", creation date = " + creationDate.getYear() + "-"  + creationDate.getMonthValue() + "-" +
                    creationDate.getDayOfMonth() + ", location from = " + from.toString() + ", location to = " +
                    to.toString() + ", distance = " + distance + ", owner = " + owner + "]");
        }
    }
}