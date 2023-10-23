package com.nmu.evos.execute;

import com.nmu.evos.ER;
import com.nmu.evos.simulator.*;
import com.nmu.evos.simulator.Point;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Tracker {
    private final KheperaSimulator simulator;
    private final State start_state;
    private ArrayList<State> post_states;
    public final Grids grids;
    public Tracker(Point initial_position, double orientation) {
        this.start_state = new State(initial_position.x, initial_position.y, orientation);
        this.simulator = new KheperaSimulator(start_state);
        this.post_states = new ArrayList<>();
        this.grids = null;
        post_states.add(start_state);
    }

    public Tracker(Point initial_position, double orientation, Point region_start, Point region_end) {
        this.start_state = new State(initial_position.x, initial_position.y, orientation);
        this.simulator = new KheperaSimulator(start_state);
        this.post_states = new ArrayList<>();
        this.grids = new Grids(region_start, region_end, 1, 1, 70);
        post_states.add(start_state);
    }

    public State getApproximatePosition(Command command) {
        KheperaState state = simulator.getNextKheperaState(command);
        post_states.add(state.position);
        System.out.println(Arrays.toString(state.sensorReadings));
        return state.position;
    }
    public void showVisualPath() {
        VisualFrame vis;
        if (grids != null) vis = new VisualFrame(50, 50, 1000, 1000, new ArrayList<>(), 5, new Point(0, 0), new Point((int) start_state.sx, (int) start_state.sy) , simulator.targetRadius, simulator.robotRadius, grids.getGrids());
        else vis = new VisualFrame(50, 50, 1000, 1000, new ArrayList<>(), 5, new Point(0, 0), new Point((int) start_state.sx, (int) start_state.sy) , simulator.targetRadius, simulator.robotRadius);
        vis.setPath(post_states, "Number States: " + post_states.size());

        Thread t = new Thread(vis);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void showVisualPosition() {
        VisualFrame vis;
        if (grids != null) vis = new VisualFrame(50, 50, 1000, 1000, new ArrayList<>(), 5, new Point(0, 0), new Point((int) start_state.sx, (int) start_state.sy) , simulator.targetRadius, simulator.robotRadius, grids.getGrids());
        else vis = new VisualFrame(50, 50, 1000, 1000, new ArrayList<>(), 5, new Point(0, 0), new Point((int) start_state.sx, (int) start_state.sy) , simulator.targetRadius, simulator.robotRadius);

        ArrayList<State> recent_most_state = new ArrayList<>();
        recent_most_state.add(post_states.get(post_states.size() - 1));
        vis.setPath(recent_most_state, "Number States: " + recent_most_state.size());

        Thread t = new Thread(vis);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public class Grid extends Region {
        private boolean forbidden;
        private final int row;
        private final int col;
        private final int target_inner_circle_radius_threshold;
        private final List<Point[]> lines;
        public Grid(Point start, Point end, int row, int col, boolean forbidden, int target_inner_circle_radius_threshold) {
            super(start, end);
            this.forbidden = forbidden;
            this.row = row;
            this.col = col;
            this.target_inner_circle_radius_threshold = target_inner_circle_radius_threshold;
            this.lines = new ArrayList<>();

            Point bottom_right_vertex = new Point(end.x, start.y);
            Point top_left_vertex = new Point(start.x, end.y);

            lines.add(new Point[] {start, top_left_vertex});
            lines.add(new Point[] {start, bottom_right_vertex});
            lines.add(new Point[] {top_left_vertex, end});
            lines.add(new Point[] {end, bottom_right_vertex});
        }
        public boolean isRestricted() {
            return forbidden;
        }
        @Override
        public String toString() {
            return "Grid{" +
                    "row=" + row +
                    ", col=" + col +
                    '}';
        }
        public List<Point[]> getLines() {
            return lines;
        }

        public double getTargetCircularInnerBoundaryRadius() {
            double target_circular_boundary_radius = getMinCircularInnerBoundaryRadius() - target_inner_circle_radius_threshold;
            return Math.max(1, target_circular_boundary_radius);
        }
    }
    
    public class Grids extends Region{
        // The boundaries of the grids to be divided
        private final Point area_start;
        private final Point area_end;
        // the number of rows and columns for the grids
        public final int rows;
        public final int columns;
        private final Grid[][] grids;
        public Grids(Point area_start, Point area_end, int rows, int columns, int target_inner_circle_radius_threshold) {
            super(area_start, area_end);
            this.area_start = area_start;
            this.area_end = area_end;
            this.rows = rows;
            this.columns = columns;
            this.grids = new Grid[rows][columns];

            // The dimensions of each grid
            double grid_width = (area_end.x - area_start.x) / columns;
            double grid_height = (area_end.y - area_start.y) / rows;

            // The rows * columns grids are stored in the grids array
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    Point start = new Point(area_start.x + j * grid_width, area_start.y + i * grid_height);
                    Point end = new Point(area_start.x + (j + 1) * grid_width, area_start.y + (i + 1) * grid_height);
                    grids[i][j] = new Grid(start, end, i, j, false, target_inner_circle_radius_threshold);
                }
            }
        }
        public Grid[][] getGrids() {
            return grids;
        }
    }
    public abstract class Region {
        protected final Point start;
        protected final Point end;
        public Region(Point start, Point end) {
            this.start = start;
            this.end = end;
        }

        public Point center() {
            return new Point((end.x - start.x) / 2 + start.x, (end.y - start.y) / 2 + start.y);
        }
        public boolean contains(double x, double y) {
            return x >= start.x && x < end.x && y >= start.y && y < end.y;
        }
        public double distanceFromCenter(double x, double y) {
            return Math.sqrt(Math.pow(center().x - x, 2) + Math.pow(center().y - y, 2));
        }

        public double getMinCircularInnerBoundaryRadius() {
            double min_distance = distanceFromCenter(start.x, (start.y + end.y) / 2);
            min_distance = Math.min(min_distance, distanceFromCenter((start.x + end.x) / 2, end.y));
            min_distance = Math.min(min_distance, distanceFromCenter(end.x, (start.y + end.y) / 2));
            min_distance = Math.min(min_distance, distanceFromCenter((start.x + end.x) / 2, start.y));
            return min_distance;
        }
    }

    public static class VisualGrid {
        public final Grid[][] grids;
        private final VisualFrame frame;
        public VisualGrid(Grid[][] grids, VisualFrame frame) {
            this.grids = grids;
            this.frame = frame;
        }
        public void drawGrids(Graphics graphics, int scale) {
            for (Grid[] x : grids) {
                for (Grid grid: x) {
                    drawGrid(graphics, grid, scale);
                    if (grid.isRestricted()) graphics.setColor(new Color(1.0F, 0.0F, 0.0F, 0.5F));
                    else graphics.setColor(new Color(0.0F, 1.0F, 0.0F, 0.5F));
                    drawGridCircularBoundary(graphics, grid, scale);
                }
            }
        }
        public void drawGridCircularBoundary(Graphics graphics, Grid grid, int scale) {
            if (grid.isRestricted()) graphics.setColor(new Color(1.0F, 0.0F, 0.0F, 0.3F));
            else graphics.setColor(new Color(0.0F, 1.0F, 0.0F, 0.3F));
            int x = (int) grid.center().x;
            int y = (int) grid.center().y;
            int radius = (int) grid.getTargetCircularInnerBoundaryRadius() * (2/ scale);
            graphics.fillOval(x * (2/scale) - radius + frame.getWidth() / 2, -y * (2/scale) - radius + frame.getHeight() / 2, radius * 2, radius * 2);
        }
        public void drawGrid(Graphics graphics, Grid grid, int scale) {
            if (grid.isRestricted()) graphics.setColor(new Color(1.0F, 0.0F, 0.0F, 1.0F));
            else graphics.setColor(new Color(0.0F, 1.0F, 0.0F, 1.0F));
            for (Point[] line: grid.getLines()) {
                int x1 = (int) line[0].x;
                int y1 = (int) line[0].y;
                int x2 = (int) line[1].x;
                int y2 = (int) line[1].y;
                drawLine(graphics, scale, x1, y1, x2, y2);
            }
        }
        public void drawLine(Graphics graphics, int scale, int x1, int y1, int x2, int y2) {
            graphics.drawLine(frame.getWidth() / 2 + x1 *(2 / scale), frame.getHeight() / 2 - y1*(2/ scale), frame.getWidth() / 2 + x2*(2 / scale), frame.getHeight() / 2 - y2*(2 / scale));
        }
    }

}
