package dhbw;

import java.util.Map;
import java.util.Optional;

class Main {
    static void main(String[] args) {
        System.out.print("Hello and welcome!");

    }
}

enum VertexDirection {
    TOP_LEFT, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, LEFT
}

enum EdgeDirection {
    TOP, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, TOP_LEFT
}

record HexPosition(int q, int r) {
} // Axial coordinates

record HexTile(HexPosition position, Resource resource, int number) {
}

record VertexPosition(HexPosition hex, VertexDirection direction) {
}

record EdgePosition(HexPosition hex, EdgeDirection direction) {
}

record Board(Map<HexPosition, HexTile> tiles, Map<VertexPosition, Building> vertexBuildings,
        Map<EdgePosition, Road> edgeRoads) {

    Board {
        tiles = Map.copyOf(tiles);
        vertexBuildings = Map.copyOf(vertexBuildings);
        edgeRoads = Map.copyOf(edgeRoads);
    }

    Optional<HexTile> tileAt(HexPosition pos) {
        return Optional.ofNullable(tiles.get(pos));
    }

    Optional<Building> buildingAt(VertexPosition pos) {
        return Optional.ofNullable(vertexBuildings.get(pos));
    }

    Optional<Road> roadAt(EdgePosition pos) {
        return Optional.ofNullable(edgeRoads.get(pos));
    }

    Board placeBuilding(VertexPosition pos, Building building) {
        if (vertexBuildings.containsKey(pos))
            throw new IllegalStateException("Position already occupied");
        var updated = new java.util.HashMap<>(vertexBuildings);
        updated.put(pos, building);
        return new Board(tiles, updated, edgeRoads);
    }

    Board placeRoad(EdgePosition pos, Road road) {
        if (edgeRoads.containsKey(pos))
            throw new IllegalStateException("Edge already occupied");
        var updated = new java.util.HashMap<>(edgeRoads);
        updated.put(pos, road);
        return new Board(tiles, vertexBuildings, updated);
    }
}
