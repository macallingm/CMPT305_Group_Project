
COURSE: CMPT-305 - Introduction to Object-Oriented Programming
DEVELOPERS: Mahar Macalling, Claire Smith, Matt Beaulieu
DATE: Dec 07, 2025

APP NAME: Edmonton School Catchment Zones
REQUIRED FILES:
    Property_Assessment_Data_2025.csv
    Edmonton_Public_School_Board_2025.csv

DESCRIPTION: This application allows the user to display Edmonton Public School catchment zones, based on school type
(example: JS - Gr 7-12 , EJS - K to Gr 12), and identify the boundary of each catchment zone on top of a map
of Edmonton. Filtering school type will also display the location of each school of that type on the map indicated by
red circle objects. Clicking on a catchment zone will provide the user with a pop-up window indicating the schools
within the catchment zone or zones, if there is overlap. Clicking into a school, within the pop-up, details
statistics based on the residential properties caught within that individual catchment zone. Residential properties are
defined as those with a 100% residential assessment class, no garage or suite number, and an assessed value greater
than zero.

HOW TO RUN:
    1. Execute Maven Goal - mvn exec:java
    2. Click [School Type] filter to see and access catchment zones of that school type.
    3. Hovering over a school's physical location (as indicated by red circle objects) will display the school's name,
    type, grade range, and address.
    4. Clicking on a catchment zone provides the user with a pop-up showing the school within that catchment zone. This
    feature is helpful when there are more than one filter turned on and catchment zones are overlapping.
    Note: the user is unable to click on a catchment zone through the hover pop-up.
    5. Clicking the school card in the Overlapping Catchment Zones pop-up provides the user with summary statistics of
    the assessed values of properties within that catchment zone.
    6. The user may turn on/off filters freely to see catchment zones. Note: many schools (example AB School for the
    Deaf) do not have catchment zones. Many of these can be seen with the All Grades filter or the Specialized Program
    filter turned on.

