# DailyPlanner App Documentation

## Project Overview

DailyPlanner is an Android productivity application built with Kotlin to help users plan their days in hourly segments and manage tasks efficiently. The app combines a visual calendar view with interactive time slots (6:00–23:00) and categorized task lists (Overdue, Today, Upcoming, Done). Its responsive design ensures usability across all Android device sizes, promoting seamless daily scheduling and task tracking.


## Daily Planner View

- **Date Navigation**: Buttons to move by day, month, or year ensure quick date changes without manual input.
    
- **Calendar Grid**: Shows the current month’s days, previous/next month days in gray, and highlights today’s date in purple for easy reference.
    
- **Time Slots**: Below the calendar, hours from 6:00 through 23:00 are displayed in a scrollable list—users tap an empty slot to schedule tasks and view existing assignments.
    
- **Responsive Layout**: ConstraintLayout and RecyclerView adapt elements to screen size, maintaining alignment and readability on all devices.

## Task Management View

- **Task Categories**: Four sections—Overdue (red), Today (green), Upcoming (blue), Done (purple)—allow users to filter tasks by status at a glance.
    
- **Add Task**: Floating action button opens a dialog to enter title, description, and due date, then saves to local Room database for offline access.
    
- **Toggle Completion**: Tapping a task moves it between active and done states, automatically reclassifying it based on the selected date.
    
- **Empty States**: Informative messages display when no tasks exist in a category, encouraging task creation without leaving blank screens.


    ![TimeBloc](https://github.com/user-attachments/assets/9a60b89d-6e4a-45f2-813f-1df5353700c6)


## Features & Functions

## Calendar Functions

- View monthly grid with week numbers
    
- Navigate by day, month, year
    
- Tap dates to switch daily schedules
    

## Scheduling Functions

- Hourly time slots (6:00–23:00)
    
- Highlight current hour slot
    
- Tap empty slots to schedule tasks
    


## Task Functions

- Add tasks with due date and time
    
- Categorize into Overdue/Today/Upcoming/Done
    
- Toggle completion status


