# Sample Map (With Direction)

This is a sample Android navigation app that guides a user to his/her desired destination in a map.

*Note: In case the Google API key on this project does not work, provide your own API key in the strings.xml file.*

## App Features
![sc_Sample Maps](https://user-images.githubusercontent.com/12168036/60862219-804a9b80-a24f-11e9-9e32-5834368c696c.jpg)
* Provides street route from current location to destination point.
* A guide is shown (e.g. "Turn right onto Brixton St.") as the current location moves.
* The route is changed once the current location moves to wrong direction.

## Code Features
* Uses FusedLocationProviderClient (instead of the deprecated FusedLocationProviderApi) for interacting with the fused location provider.
* Uses Google Maps API and Directions API.
