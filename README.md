Title: I created a Salon Appointment Booking System with Java Swing and MySQL! Looking for feedback.
Hello everyone!

I'm excited to share a desktop application I've been working on: a Salon Appointment Booking System built entirely with Java. I wanted to practice my skills with GUI development and database integration, and this felt like a practical and interesting project.

Project Overview
It's a classic client-server style application that allows users to book appointments and staff to manage their schedules. The GUI is built using Java Swing, and it connects to a MySQL database to handle all the data.

Key Features
Dual User Roles: The system has two distinct interfaces: one for regular users (customers) and another for salon staff.

User Dashboard:
Secure login and signup for users.
Book a new appointment, choosing from available staff.
View a list of all personal bookings.
Update the time/date of an existing appointment.
Cancel/delete a booking.

Staff Dashboard:
Secure login and signup for staff.
View all appointments assigned to them.
Update their personal profile (name, specialization, available hours).
Delete their own staff account.

Dynamic Theme Switching: Includes a "Toggle Theme" button that switches the entire application between a light and a dark mode aesthetic without a restart.

Custom UI: I've used custom panels with background images and styled buttons with hover effects to create a more modern look than standard Swing components.

Conflict Resolution: The system checks the database to prevent double-booking a staff member for the same date and time.


Tech Stack
Language: Java
UI: Java Swing
Database: MySQL (with JDBC for connection).
