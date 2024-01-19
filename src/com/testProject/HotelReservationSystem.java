package com.testProject;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

    public class HotelReservationSystem {

    private static final String url = "jdbc:postgresql://localhost:5432/postgres";
    private static final String name = "postgres";
    private static final String pass = "0000";

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        try {
            //load Drivers
            Class.forName("org.postgresql/Driver");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            //create connection
            Connection con = DriverManager.getConnection(url, name, pass);
            while (true) {
                System.out.println("\nHOTEL RESERVATION SYSTEM");
                System.out.println("1. New Reservation");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservation");
                System.out.println("5. Delete Reservation");
                System.out.println("0. Exit");
                System.out.print("choose an option: ");
                int choose = scan.nextInt();

                switch (choose) {
                    case 1:
                        newReservation(con, scan);
                        break;
                    case 2:
                        viewReservation(con);
                        break;
                    case 3:
                        getRoomNumber(con, scan);
                        break;
                    case 4:
                        updateReservation(con, scan);
                        break;
                    case 5:
                        deleteReservation(con, scan);
                        break;
                    case 0:
                        exit();
                        scan.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void newReservation(Connection con, Scanner scan) throws IOException {

        System.out.print("Enter The Guest name: ");
        String guestName = scan.next();
        System.out.print("Enter Room Number: ");
        int roomNo = scan.nextInt();
        System.out.print("Enter Contact Number: ");
        String contact = scan.next();
        System.out.print("Enter Email: ");
        String Email = null;
        char ignore = (char) System.in.read();
        if (ignore != '\n') {
            Email = scan.next();
        }
        System.out.println("Select room: \n1. single Bedroom --- 1000.00 / Day\n2. double BedRoom --- 1500.00 / Day");
        String selectRoom;
        String roomType = null;
        long price;
        int noOfDays;

        while (true) {
            selectRoom = scan.next();
            if (selectRoom.equals("1")) {
                roomType = "Single";
                System.out.print("How many Days want to stay: ");
                noOfDays = scan.nextInt();
                price = (long) (noOfDays * 1000.00);
                break;
            }
            else if (selectRoom.equals("2")) {
                roomType = "Double";
                System.out.print("How many Days want to stay: ");
                noOfDays = scan.nextInt();
                price = (long) (noOfDays * 1500.00);
                break;
            }
            else
                System.out.println("Invalid input! Try Again...");
        }

        //CheckIn date
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        String checkIn = localDateTime.format(myFormatObj);

        //CheckOut date
        LocalDateTime ldtAddDays = localDateTime.plusDays(noOfDays);
        String checkOut = ldtAddDays.format(myFormatObj);

        String sqlQ = "INSERT INTO public.\"HotelDB\"(\n" +
                "\"GuestName\", \"RoomNo\", \"RoomType\", \"Contact\", \"Email\", \"CheckIn\", \"CheckOut\", \"NoOfDays\", \"Price\")\n" +
                "\tVALUES ( '" + guestName + "', " + roomNo + ", '" + roomType + "', '" + contact + "', '" + Email + "', to_date('" +
        checkIn + "', 'dd-mm-yyyy'), to_date('" + checkOut + "', 'dd-mm-yyyy'), " + noOfDays + ", " + price +");";


        /*String sqlQ = "INSERT INTO HotelDB(GuestName, RoomNo, RoomType, Contact, Email, CheckIn, CheckOut, NoOfDays, Price)" +
                            " values( '" + guestName + "', " + roomNo + ", '" + roomType + "', '" + contact + "', '" + Email + "', '" +
                           checkIn + "', '" + checkOut +"', " + noOfDays + ", " + price +");";
*/
        try {
            Statement statement = con.createStatement();
            int affectedRows = statement.executeUpdate(sqlQ);

            if (affectedRows > 0)
                System.out.println("Reservation Successful!");
            else
                System.out.println("Reservation Failed");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
    }

    private static void viewReservation(Connection con) {
        String sqlQ = "SELECT \"ID\", \"GuestName\", \"RoomNo\", \"RoomType\", \"Contact\", \"Email\", \"CheckIn\", \"CheckOut\", \"NoOfDays\", \"Price\"\n" +
                "\tFROM public.\"HotelDB\";";

        try {
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQ);

            System.out.println("Current reservation:");
            System.out.println("+------+----------------------+------+--------+------------+------------+------------+------+-----------------+");
            System.out.println("| ID   | Guest Name           | RNo  | RType  | ContactNo  | CheckIn    | CheckOut   | Days | Price           |");
            System.out.println("+------+----------------------+------+--------+------------+------------+------------+------+-----------------+");

            while (resultSet.next()) {
                int reservId = resultSet.getInt("reservationID");
                String guestName = resultSet.getString("guestName");
                int roomNumber = resultSet.getInt("roomNo");
                String roomType = resultSet.getString("RoomType");
                String contactNumber = resultSet.getString("contact");
                String reservTime = resultSet.getString("CheckIn");
                String checkOut = resultSet.getString("CheckOut");
                int Days = resultSet.getInt("NoOfDays");
                String price = resultSet.getString("Price");

                System.out.printf("| %-4d | %-20s | %-4d | %-6s | %-10s | %-10s | %-10s | %-4d | %-15s |\n",
                        reservId, guestName, roomNumber, roomType, contactNumber, reservTime, checkOut, Days,price);
                System.out.println("+------+----------------------+------+--------+------------+------------+------------+------+-----------------");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void getRoomNumber(Connection con, Scanner scan) {
        System.out.print("Enter Guest Name: ");
        String GName = scan.next();
        System.out.print("Enter Contact Number: ");
        String contactNumber = scan.next();
        String sqlQ = "select * from HotelDB where GuestName = '" + GName + "' and contact = '" + contactNumber + "'";

        try {
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQ);

            if (resultSet.next()) {
                int roomNumber = resultSet.getInt("room_number");
                System.out.println("Room number for Guest " + GName + " is: " + roomNumber);
            } else {
                System.out.println("Reservation not found for the given guest name and contact number.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateReservation(Connection con, Scanner scan) {
        try {
            System.out.print("Enter reservation Id to update: ");
            int reservID = scan.nextInt();

            if (!reservationExists(con, reservID)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            System.out.print("Enter new guest name: ");
            String newGuestName = scan.next();
            System.out.print("Enter new room number: ");
            int newRoomNumber = scan.nextInt();
            System.out.print("Enter new contact number: ");
            String newContactNumber = scan.next();

            String sql = "UPDATE reservation SET guest_name = '" + newGuestName + "', " +
                    "room_number = " + newRoomNumber + ", " +
                    "contact_number = '" + newContactNumber + "' " +
                    "WHERE reservation_id = " + reservID;

            Statement statement = con.createStatement();
            int affectedRows = statement.executeUpdate(sql);
            if (affectedRows > 0)
                System.out.println("Reservation Update Successful!");
            else
                System.out.println("Reservation Update Failed");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteReservation(Connection con, Scanner scan) {
        System.out.print("Enter reservation ID to delete: ");
        int reservationId = scan.nextInt();
        String sqlQ = "delete from reservation where reservation_id = " + reservationId;

        try {
            Statement statement = con.createStatement();
            int affectedRows = statement.executeUpdate(sqlQ);

            if (affectedRows > 0)
                System.out.println("Reservation deleted Successful!");
            else
                System.out.println("Reservation deletion Failed");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 3;
        while (i != 0) {
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For Using Hotel Reservation System!!!");
    }

    private static boolean reservationExists(Connection con, int reservID) {
        try {
            String sql = "SELECT reservation_id FROM reservation WHERE reservation_id = " + reservID;

            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.next(); // If there's a result, the reservation exists
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}

