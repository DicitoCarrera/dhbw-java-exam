package dhbw;

import dhbw.Building.Settlement;

public sealed interface Building permits Settlement, City, Road {
 record Settlement(VertexPosition position) implements Building {
 }

 record City(VertexPosition position) implements Building {
 }

 record Road(EdgePosition fromTo) implements Building {
 }

 VertexPosition position(); // We'll define Position shortly

}
