// --- Coordinate Types ---

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import dhbw.test.HarborType.GenericHarbor;
import dhbw.test.HarborType.SpecificHarbor;
import dhbw.test.ResourceType.Brick;
import dhbw.test.ResourceType.Lumber;
import dhbw.test.ResourceType.Ore;
import dhbw.test.ResourceType.Sheep;
import dhbw.test.ResourceType.Wheat;
import dhbw.test.TileType.Desert;
import dhbw.test.TileType.Fields;
import dhbw.test.TileType.Forest;
import dhbw.test.TileType.Hills;
import dhbw.test.TileType.Mountains;
import dhbw.test.TileType.Pasture;

public record HexCoord(int q, int r) {
} // axial hex coordinate

public record VertexCoord(int id) {
} // placeholder for a real spatial model

public record EdgeCoord(int id) {
} // placeholder for a real spatial model

// --- Resource Types ---
public sealed interface ResourceType permits Brick, Lumber, Ore, Sheep, Wheat {
    record Brick() implements ResourceType {
    }

    record Lumber() implements ResourceType {
    }

    record Ore() implements ResourceType {
    }

    record Sheep() implements ResourceType {
    }

    record Wheat() implements ResourceType {
    }
}

// --- Tile Types ---
public sealed interface TileType permits Desert, Forest, Hills, Mountains, Fields, Pasture {
    record Desert() implements TileType {
        @Override
        public Optional<ResourceType> resource() {
            return Optional.empty();
        }
    }

    record Forest() implements TileType {
        @Override
        public Optional<ResourceType> resource() {
            return Optional.of(new ResourceType.Lumber());
        }
    }

    record Hills() implements TileType {
        @Override
        public Optional<ResourceType> resource() {
            return Optional.of(new ResourceType.Brick());
        }
    }

    record Mountains() implements TileType {
        @Override
        public Optional<ResourceType> resource() {
            return Optional.of(new ResourceType.Ore());
        }
    }

    record Fields() implements TileType {
        @Override
        public Optional<ResourceType> resource() {
            return Optional.of(new ResourceType.Wheat());
        }
    }

    record Pasture() implements TileType {
        @Override
        public Optional<ResourceType> resource() {
            return Optional.of(new ResourceType.Sheep());
        }
    }

    Optional<ResourceType> resource();
}

// --- Tile ---
public record HexTile(HexCoord coord, TileType tileType, OptionalInt numberToken) {
}

// --- Harbor Types ---
public sealed interface HarborType permits GenericHarbor, SpecificHarbor {
    record GenericHarbor() implements HarborType {
    }

    record SpecificHarbor(ResourceType resource) implements HarborType {
    }
}

public record Harbor(EdgeCoord edge, HarborType type) {
}

// --- Structures ---
public sealed interface Structure permits Structure.Settlement, Structure.City {
    record Settlement(VertexCoord position) implements Structure {
        @Override
        public StructureType type() {
            return new StructureType.SettlementType();
        }
    }

    record City(VertexCoord position) implements Structure {
        @Override
        public StructureType type() {
            return new StructureType.CityType();
        }
    }

    VertexCoord position();

    StructureType type();
}

// --- Road ---
public record Road(EdgeCoord position) {
}

public sealed interface StructureType permits StructureType.SettlementType, StructureType.CityType {
    record SettlementType() implements StructureType {
    }

    record CityType() implements StructureType {
    }
}

// --- Player ---
public record Player(
        String name,
        Map<ResourceType, Integer> resources,
        List<Structure> ownedStructures,
        List<Road> ownedRoads) {

    public Player withAddedStructure(Structure structure) {
        List<Structure> updatedStructures = new ArrayList<>(ownedStructures);
        updatedStructures.add(structure);
        return new Player(name, resources, List.copyOf(updatedStructures), ownedRoads);
    }

    public Player withAddedRoad(Road road) {
        List<Road> updatedRoads = new ArrayList<>(ownedRoads);
        updatedRoads.add(road);
        return new Player(name, resources, ownedStructures, List.copyOf(updatedRoads));
    }
}

// --- Board ---
public record Board(
        List<HexTile> tiles,
        List<Harbor> harbors,
        Map<VertexCoord, Structure> structures,
        Map<EdgeCoord, Road> roads) {
    public Board withStructure(VertexCoord coord, Structure structure) {
        Map<VertexCoord, Structure> updated = new HashMap<>(structures);
        updated.put(coord, structure);
        return new Board(tiles, harbors, Map.copyOf(updated), roads);
    }

    public Board withRoad(EdgeCoord coord, Road road) {
        Map<EdgeCoord, Road> updated = new HashMap<>(roads);
        updated.put(coord, road);
        return new Board(tiles, harbors, structures, Map.copyOf(updated));
    }

    public Board moveRobber(HexCoord newPosition) {
        return new Board(tiles, harbors, structures, roads);
    }
}

// --- Factory ---
public static Board createInitialBoard() {
    List<TileType> tileTypes = List.of(
            new TileType.Desert(),
            new TileType.Forest(), new TileType.Forest(), new TileType.Forest(), new TileType.Forest(),
            new TileType.Hills(), new TileType.Hills(), new TileType.Hills(),
            new TileType.Mountains(), new TileType.Mountains(), new TileType.Mountains(),
            new TileType.Fields(), new TileType.Fields(), new TileType.Fields(), new TileType.Fields(),
            new TileType.Pasture(), new TileType.Pasture(), new TileType.Pasture(), new TileType.Pasture());

    List<Integer> numberTokens = List.of(
            2, 3, 3, 4, 4, 5, 5,
            6, 6, 8, 8,
            9, 9, 10, 10, 11, 11, 12);

    List<HexCoord> coords = axialSpiralCoords(2); // 19 coords
    Collections.shuffle(tileTypes);
    Collections.shuffle(numberTokens);

    List<HexTile> tiles = new ArrayList<>();
    Iterator<TileType> tileIt = tileTypes.iterator();
    Iterator<Integer> numIt = numberTokens.iterator();

    for (HexCoord coord : coords) {
        TileType type = tileIt.next();
        OptionalInt number = (type instanceof TileType.Desert) ? OptionalInt.empty() : OptionalInt.of(numIt.next());
        tiles.add(new HexTile(coord, type, number));
    }

    Optional<HexCoord> desertCoord = tiles.stream()
            .filter(t -> t.tileType() instanceof TileType.Desert)
            .map(HexTile::coord)
            .findFirst();

    List<Harbor> harbors = List.of(
            new Harbor(new EdgeCoord(0), new HarborType.GenericHarbor()),
            new Harbor(new EdgeCoord(1), new HarborType.GenericHarbor()),
            new Harbor(new EdgeCoord(2), new HarborType.GenericHarbor()),
            new Harbor(new EdgeCoord(3), new HarborType.GenericHarbor()),
            new Harbor(new EdgeCoord(4), new HarborType.SpecificHarbor(new ResourceType.Lumber())),
            new Harbor(new EdgeCoord(5), new HarborType.SpecificHarbor(new ResourceType.Brick())),
            new Harbor(new EdgeCoord(6), new HarborType.SpecificHarbor(new ResourceType.Ore())),
            new Harbor(new EdgeCoord(7), new HarborType.SpecificHarbor(new ResourceType.Wheat())),
            new Harbor(new EdgeCoord(8), new HarborType.SpecificHarbor(new ResourceType.Sheep())));

    return new Board(tiles, harbors, Map.of(), Map.of());
}

// --- Helper ---
public static List<HexCoord> axialSpiralCoords(int radius) {
    List<HexCoord> coords = new ArrayList<>();
    for (int q = -radius; q <= radius; q++) {
        int r1 = Math.max(-radius, -q - radius);
        int r2 = Math.min(radius, -q + radius);
        for (int r = r1; r <= r2; r++) {
            coords.add(new HexCoord(q, r));
        }
    }
    return coords;
}

// --- Game Phases ---
public sealed interface GamePhase permits GamePhase.SetupPhase, GamePhase.PlayPhase, GamePhase.EndPhase {

    // Abstract method to define phase-specific logic
    void executePhase(Game game);

    // --- Setup Phase ---
    public record SetupPhase() implements GamePhase {
        @Override
        public void executePhase(Game game) {
            System.out.println("Executing Setup Phase");

            // Example setup logic
            // 1. Players select starting positions for structures.
            // 2. Initial resource allocation.

            // Transition to PlayPhase
            game.transitionTo(new PlayPhase());
        }
    }

    // --- Play Phase ---
    public record PlayPhase() implements GamePhase {
        @Override
        public void executePhase(Game game) {
            System.out.println("Executing Play Phase");

            // Example play logic:
            // 1. Players take turns, roll dice, collect resources, build structures.
            // 2. Handle player actions, including road and settlement placements.

            // For simplicity, let's transition to EndPhase when a condition is met (like 10
            // points reached).
            if (game.getCurrentPlayerScore() >= 10) {
                game.transitionTo(new EndPhase());
            }
        }
    }

    // --- End Phase ---
    public record EndPhase() implements GamePhase {
        @Override
        public void executePhase(Game game) {
            System.out.println("Executing End Phase");

            // Example end game logic:
            // 1. Check if someone has won (e.g., based on score).
            // 2. Declare the winner and end the game.
        }
    }
}
