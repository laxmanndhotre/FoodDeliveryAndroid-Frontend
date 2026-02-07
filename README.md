# Android App Summary: FoodGram Delivery

## Overview
**FoodGram Delivery** is an Android application designed for delivery personnel to manage orders, track earnings, and maintain their profile. It is built using **Java** and follows a standard architecture with fragments for main screens.

## Architecture
-   **Language**: Java
-   **Navigation**: Single Activity (`HomeActivity`) using `ViewPager2` combined with `BottomNavigationView`.
-   **Network**: Retrofit for REST API communication.
-   **Design**: Material Design Components, Shimmer effects for loading states, and custom UI elements (e.g., `SquigglySwipeRefreshLayout`).

## Key Features

### 1. Orders Management (`OrdersFragment`)
The core feature where delivery persons interact with orders.
-   **Tabs**:
    -   **Available Orders**: Lists orders waiting to be picked up. Filters include 'Pending', 'Confirmed', 'Preparing'.
    -   **My Orders**: Lists orders assigned to the logged-in user.
-   **Separation**: "My Orders" are now categorized into:
    -   **Active Orders**: Orders in progress (Picked Up, Out for Delivery).
    -   **Past Orders**: Completed (Delivered) or Cancelled orders.
-   **Actions**:
    -   **Accept Order**: Assigns an available order to the user.
    -   **Mark Delivered**: Updates status to 'DELIVERED' (requires 'Out for Delivery' status).
    -   **Cancel/Unassign**: Reverts order acceptance (if not yet delivered).

### 2. Analytics (`AnalyticsFragment`)
Provides a dashboard for performance tracking.
-   **Metrics**:
    -   Total Earnings (Calculated as ₹20 per delivered order).
    -   Completed Deliveries count.
    -   Pending Deliveries count.
    -   Best Day (Date with highest earnings).
-   **Visuals**: Displays stats in a card-based layout.

### 3. Profile Management (`ProfileFragment`)
View and edit personal details.
-   **View Profile**: Name, Email, Phone, Vehicle Number, Operating Area, Status.
-   **Edit Profile**: Allows updating Name, Phone, Vehicle, and Area.
-   **Logout**: Clears the JWT token and redirects to Login.

## Technical Details
-   **Authentication**: JWT-based auth stored in `SharedPreferences` via `TokenManager`.
-   **API Clients**:
    -   `OrderApi`: Handles fetching orders, accepting, cancelling, and marking delivered.
    -   `DeliveryApi`: Handles profile creation and updates.
-   **UI Components**:
    -   `OrdersAdapter`: Complex adapter handling headers ("Active", "Past") and delivery items.
    -   `SquigglySwipeRefreshLayout`: Custom pull-to-refresh animation.
    -   `BounceTouchListener`: Adds touch feedback to buttons.

## Recent Updates
-   **Refactored Cancel Logic**: Cancelling an order now unassigns it instead of permanently cancelling it, making it available for others.
-   **UI Separation**: "My Orders" list is now sectioned for better organization.
-   **Delivery Flow Fix**: Allowed marking orders as delivered from "Out for Delivery" state.
