package com.nmu.evos;

import com.nmu.evos.execute.Tracker;
import com.nmu.evos.simulator.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

public class ER2 {
    public static Normalisation normalisation;
    private static final double[] sensor_range = new double[] {0, 3900};
    static {
        // Normalisation
        normalisation = new Normalisation(input -> {
            // normaliser - input
            double[] xy_pos = Normalisation.normalise(Arrays.copyOfRange(input, 0, 2), 0, 150, -1, 1); // Assuming xy plane ranges from (0,0) to (150,150)
            double[] sensors = Normalisation.normalise(Arrays.copyOfRange(input, 2, 11), sensor_range[0], sensor_range[1], -1, 1);
            return new double[] {
                    xy_pos[0],
                    xy_pos[1],
                    sensors[0],
                    sensors[1],
                    sensors[2],
                    sensors[3],
                    sensors[4],
                    sensors[5],
                    sensors[6],
                    sensors[7],
                    sensors[8]
            };
            // de_normaliser - output
        }, input -> Normalisation.de_normalise(input, 8_000, 17_500, -1, 1));
    }
    private static final int COMMAND_COUNT = 50;
    public StringBuilder training_info = new StringBuilder();
    private final Random random = new Random(988687678);
    private final int POP_SIZE = 500;
    private final int GENERATIONS = 20;
    private final int INPUT_NEURON_COUNT = 11;
    private final int HIDDEN_LAYER_NEURON_COUNT = 27;
    private final int OUTPUT_NEURON_COUNT = 2;
    private final double MUT_RATE = 0.5;
    private final double MUT_STD_DEV = 0.1;
    private int generation_counter = 0;
    private Individual[] population = new Individual[POP_SIZE];
    public final Point[] start_end_pos = new Point[] {new Point(150, 150), new Point(0, 0)};
    private final double robot_initial_orientation = getFacingOrientation(new Point(0, 0), new Point(150, 150)) + 10;
    public class Individual {
        public double fitness;
        private Supplier<String> info;
        public LSTMNeuralNetwork genome;
        private Individual() {
            this.genome = new LSTMNeuralNetwork(INPUT_NEURON_COUNT, HIDDEN_LAYER_NEURON_COUNT, OUTPUT_NEURON_COUNT);
        }
        private double getFitness() {
            return fitness;
        }
        @Override
        public String toString() {
            return (info != null ? info.get() : "print_fitness not defined.") + " -> " + genome;
        }
    }
    public void train() throws FileNotFoundException {
        training_info.append("\n").append("\t\t\t+=+=+=+=+=+=+=+=+=+=+=+=+=+=+= LOG +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=").append("\n");
        initialisePopulation();
        for (; generation_counter < GENERATIONS; generation_counter++) {
            Individual[] offspring_population = new Individual[POP_SIZE];
            int j = 0;
            while (j < POP_SIZE - 1) {
                Individual first_parent = tournamentSelect();
                Individual second_parent = tournamentSelect();
                Individual offspring = uniformCrossover(first_parent, second_parent);
                gaussianMutation(offspring);

                evaluateFitness(offspring);
                offspring_population[j] = offspring;
                j++;
            }

            Individual elite = Arrays.stream(population)
                    .sorted(Comparator.comparingDouble(individual -> ((Individual) individual).fitness).reversed())
                    .toArray(Individual[]::new)[0];

            offspring_population[j] = elite;

            offspring_population = Arrays.stream(offspring_population)
                    .sorted(Comparator.comparingDouble(individual -> ((Individual) individual).fitness).reversed())
                    .toArray(Individual[]::new);

            population = offspring_population;

            if(generation_counter % 10 == 0) {
                System.out.println("GENERATION = " + generation_counter + " / " + GENERATIONS);
                training_info.append("GENERATION = ").append(generation_counter).append(" / ").append(GENERATIONS).append("\n");
                displayTopN(population, 10);
                if (generation_counter % 100 == 0 & generation_counter != 0) saveBest(population[0], "er/best.er");
            }
        }

        System.out.println();
        System.out.println("\nBEST:");
        System.out.println(population[0]);
        //saveBest(population[0], "er/best.er");
        simulate(population[0], population[0].genome.hidden_layer_activation_function, population[0].genome.output_layer_activation_function, new Random(seed));
    }
    private void displayTopN(Individual[] population, int n) {
        StringBuilder temp = new StringBuilder();

        temp.append("TOP ").append(n).append(":").append("\n");
        Arrays.stream(population).limit(n).forEach(s -> temp.append(s).append("\n"));

        System.out.println(temp);
        training_info.append(temp);
    }
    public long seed = 879869761;
    public void evaluateFitness(Individual individual) {
        if (generation_counter % 10 == 0 && generation_counter != 0) seed++;
        Random rnd = new Random(seed);
        rnd.nextDouble();
        double fitness = 0;
        double acc_outside_of_region_penalty = 0;
        double acc_collision_penalty = 0;

        Point start = start_end_pos[0];
        Point end = start_end_pos[1];

        Tracker tracker = new Tracker(start, robot_initial_orientation, new Point(0, 0), new Point(150, 150));

        for (int rounds = 0; rounds < 5; rounds++) {
            ArrayList<Point> obstacles = randomPoints(rnd, 20, 150, 150);

            State start_state = new State(start.x, start.y, robot_initial_orientation);
            KheperaSimulator khepera_simulator = new KheperaSimulator(obstacles, start_state);

            KheperaState state = khepera_simulator.getNextKheperaState(new Command(0, 0, 800));

            // Initial state
            double[] normalised_xy = Normalisation.normalise(new double[]{start.x, start.y}, 0, 150, -1, 1);
            double[] input = new double[11];
            input[0] = normalised_xy[0];
            input[1] = normalised_xy[1];
            double[] sensors = state.sensorReadings;
            System.arraycopy(Normalisation.normalise(sensors, sensor_range[0], sensor_range[1], -1, 1), 0, input, 2, sensors.length); // Copying normalised sensor values to 'input' from index 4 onwards

            for (int i = 0; i < COMMAND_COUNT; i++) {
                double current_distance = distance(state.position.sx, state.position.sy, end.x, end.y);
                if (!tracker.grids.contains(state.position.sx, state.position.sy)) {
                    double distance_from_circular_inner_boundary = tracker.grids.distanceFromCircularInnerBoundary(state.position.sx, state.position.sy);
                    double penalty = distance_from_circular_inner_boundary / (1000.0 + distance_from_circular_inner_boundary);
                    acc_outside_of_region_penalty += penalty;
                }

                acc_collision_penalty += collisionPenalty(obstacles, new Point(state.position.sx, state.position.sy));

                fitness += 0.5 / (current_distance + 1);

                double[] output = individual.genome.fire(input, new TanHActivation(), new TanHActivation());
                double[] denormalised_output = Normalisation.de_normalise(output, 8_000, 17_500, -1, 1);

                System.out.println(Arrays.toString(output));

                state = khepera_simulator.getNextKheperaState(new Command((int) denormalised_output[0], (int) denormalised_output[1], 800));

                normalised_xy = Normalisation.normalise(new double[]{state.position.sx, state.position.sy}, 0, 150, -1, 1);

                input[0] = normalised_xy[0];
                input[1] = normalised_xy[1];
                sensors = state.sensorReadings;

                System.arraycopy(Normalisation.normalise(sensors, sensor_range[0], sensor_range[1], -1, 1), 0, input, 2, sensors.length);
            }
        }

        fitness = fitness - acc_collision_penalty - acc_outside_of_region_penalty;

        StringBuilder builder = new StringBuilder("Individual{");
        builder.append("fitness=").append(fitness).append(", ");
        builder.append("acc_collision_penalty=").append(acc_collision_penalty);
        builder.append("}");

        individual.fitness = fitness;
        individual.info = builder::toString;
    }

    public double collisionPenalty(ArrayList<Point> obstacles, Point robot_pos) {
        double penalty = 0;
        for (Point obstacle_pos : obstacles) {
            double distance = distance(robot_pos.x, robot_pos.y, obstacle_pos.x, obstacle_pos.y);
            if (distance <= KheperaSimulator.robotRadius + KheperaSimulator.obstacleRadius) { //TODO Requires finetuning for testing
                double inverse_distance = 0.6 / (distance + 1);
                penalty += inverse_distance;
            }
        }
        return penalty;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void simulate(Individual individual, Activation hidden_layer_activation, Activation output_layer_activation, Random rnd) {
        Point start = start_end_pos[0];
        Point end = start_end_pos[1];
        ArrayList<Point> obstacles = randomPoints(rnd, 20, 150, 150);
        Tracker tracker = new Tracker(start, robot_initial_orientation, new Point(0, 0), new Point(150, 150));

        State start_state = new State(start.x,start.y, robot_initial_orientation);
        KheperaSimulator khepera_simulator = new KheperaSimulator(obstacles,start_state);

        // Initial state
        double[] normalised_xy = Normalisation.normalise(new double[] {start.x, start.y}, 0, 150, -1, 1);
        double[] input = new double[11];
        input[0] = normalised_xy[0];
        input[1] = normalised_xy[1];
        double[] sensors = khepera_simulator.srs.getSensorReadings((int)start.x, (int) start.y, robot_initial_orientation, new ArrayList<>());
        System.arraycopy(Normalisation.normalise(sensors, sensor_range[0], sensor_range[1], -1, 1), 0, input, 2, sensors.length); // Copying normalised sensor values to 'input' from index 4 onwards

        ArrayList<State> post_states = new ArrayList<>();

        for (int i = 0; i < COMMAND_COUNT; i++) {
            double[] output = individual.genome.fire(input, hidden_layer_activation, output_layer_activation);
            double[] denormalised_output = Normalisation.de_normalise(output, 8_000, 17_500, -1, 1);

            KheperaState state = khepera_simulator.getNextKheperaState(new Command((int) denormalised_output[0], (int) denormalised_output[1], 800));

            post_states.add(state.position);

            normalised_xy = Normalisation.normalise(new double[] {state.position.sx, state.position.sy}, 0, 150, -1, 1);

            input[0] = normalised_xy[0];
            input[1] = normalised_xy[1];
            sensors = state.sensorReadings;

            System.arraycopy(Normalisation.normalise(sensors, sensor_range[0], sensor_range[1], -1, 1), 0, input, 2, sensors.length);
        }

        VisualFrame vis = new VisualFrame(50, 50, 1000, 1000, obstacles, 5, end, new Point((int) start_state.sx, (int) start_state.sy) , khepera_simulator.targetRadius, khepera_simulator.robotRadius, tracker.grids.getGrids());
        vis.setPath(post_states, "Number States: " + post_states.size());
        Thread t = new Thread(vis);

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Orientation with respect to 'end', e.g., if facing directly at 'end' position than 0.0 is returned
    public static double getRelativeOrientation(Point start, Point end, double o) {
        double obsolute = (-Math.atan2(end.x - start.x, end.y - start.y)) % (Math.PI * 2);
        obsolute = (obsolute / Math.PI) * 180;
        return (360 - o + obsolute + 180) % 360;
    }

    public ArrayList<Point> randomPoints(Random random,int count, int region_end_x, int region_end_y) {
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Point point = new Point(random.nextDouble() * region_end_x, random.nextDouble() * region_end_y);
            points.add(point);
        }

        return points;
    }

    public int getFacingOrientation(Point l, Point r) {
        double orientation = (-Math.atan2(l.x - r.x, l.y - r.y)) % (Math.PI * 2);
        orientation = (orientation / Math.PI) * 180;
        return (int) orientation;
    }

    private void initialisePopulation() {
        for (int i = 0; i < POP_SIZE; i++) {
            Individual individual = new Individual();
            double[] initialWeights = new double[individual.genome.countWeights()];
            int weightIndex = 0;

            // Initialize weights for W_f, U_f, b_f
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                for (int k = 0; k < INPUT_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }

            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                for (int k = 0; k < HIDDEN_LAYER_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }

            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                initialWeights[weightIndex++] = 0.0; // Initialize bias b_f to 0
            }

            // Initialize weights for W_i, U_i, b_i
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                for (int k = 0; k < INPUT_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                for (int k = 0; k < HIDDEN_LAYER_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                initialWeights[weightIndex++] = 0.0; // Initialize bias b_f to 0
            }
            // Initialize weights for W_c, U_c, b_c
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                for (int k = 0; k < INPUT_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                for (int k = 0; k < HIDDEN_LAYER_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                initialWeights[weightIndex++] = 0.0; // Initialize bias b_f to 0
            }
            // Initialize weights for W_o, U_o, b_o
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                for (int k = 0; k < INPUT_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                for (int k = 0; k < HIDDEN_LAYER_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }
            for (int j = 0; j < HIDDEN_LAYER_NEURON_COUNT; j++) {
                initialWeights[weightIndex++] = 0.0; // Initialize bias b_f to 0
            }

            // Initialize weights for V
            for (int j = 0; j < OUTPUT_NEURON_COUNT; j++) {
                for (int k = 0; k < HIDDEN_LAYER_NEURON_COUNT; k++) {
                    initialWeights[weightIndex++] = random.nextDouble() * 2 - 1;
                }
            }
            for (int j = 0; j < OUTPUT_NEURON_COUNT; j++) {
                initialWeights[weightIndex++] = 0.0; // Initialize bias c to 0
            }

            individual.genome.setWeights(initialWeights);
            evaluateFitness(individual);

            population[i] = individual;
        }
    }

    private Individual uniformCrossover(Individual first_parent, Individual second_parent) {
        double[] first_parent_weights = first_parent.genome.getWeights();
        double[] second_parent_weights = second_parent.genome.getWeights();
        double[] offspring_weights = new double[first_parent_weights.length];
        Individual offspring = new Individual();
        for (int i = 0; i < first_parent_weights.length; i++) {
            if (random.nextDouble() <= 0.5) offspring_weights[i] = first_parent_weights[i];
            else offspring_weights[i] = second_parent_weights[i];
        }
        offspring.genome.setWeights(offspring_weights);
        return offspring;
    }
    private void gaussianMutation(Individual offspring) {
        double[] weights = offspring.genome.getWeights();
        for (int i = 0; i < weights.length; i++) {
            if (random.nextDouble() <= MUT_RATE) {
                weights[i] = weights[i] + random.nextGaussian(0, MUT_STD_DEV);
            }
        }
        offspring.genome.setWeights(weights);
    }
    private Individual tournamentSelect() {
        int selection_group_count = (int) (POP_SIZE * 0.1);
        int current_select_group_count = Math.max(1, selection_group_count);

        // Initial selection
        int i = new Random().nextInt(POP_SIZE);
        current_select_group_count--;

        // Current max fitness
        Individual best = population[i];
        double max_fitness = best.fitness;

        while (current_select_group_count > 0) {
            i = new Random().nextInt(POP_SIZE);
            if (population[i].fitness > max_fitness) {
                best = population[i];
                max_fitness = best.fitness;
            }
            current_select_group_count--;
        }
        return best;
    }

    public void saveBest(Individual best, String filename) throws FileNotFoundException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = formatter.format(new Date());

        LSTMNeuralNetwork neuralNetwork = best.genome;
        double[] weights = neuralNetwork.getWeights();
        double fitness = best.getFitness();
        Supplier<String> info = best.info;

        PrintWriter writer = new PrintWriter(filename + "." + timestamp + "."  + best.genome.hidden_layer_activation_function + "." + neuralNetwork.output_layer_activation_function + ".nn");
        for (int i = 0; i < weights.length; i++) {
            writer.print(weights[i]);
            if (i != weights.length - 1) {
                writer.print(",");
            }
        }
        writer.println();
        writer.println(fitness);
        writer.println(info != null ? info.get() : "info not defined.");
        writer.println(best.genome.hidden_layer_activation_function);
        writer.println(best.genome.output_layer_activation_function);
        writer.println("Generation count: " + GENERATIONS);
        writer.println("Population size: " + POP_SIZE);
        writer.println("Input layer neuron count: " + INPUT_NEURON_COUNT);
        writer.println("Hidden layer neuron count: " + HIDDEN_LAYER_NEURON_COUNT);
        writer.println("Output layer neuron count: " + OUTPUT_NEURON_COUNT);
        writer.println("Mutation standard deviation: " + MUT_STD_DEV);
        writer.println("Mutation rate: " + MUT_RATE);
        writer.println(training_info);
        writer.close();
    }

    public Individual loadBest(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename.strip()));
        String weightsLine = scanner.nextLine();
        double[] weights = Arrays.stream(weightsLine.split(",")).mapToDouble(Double::parseDouble).toArray();
        double fitness = Double.parseDouble(scanner.nextLine());
        Supplier<String> printFitness = scanner::nextLine;

        Individual individual = new Individual();
        LSTMNeuralNetwork neuralNetwork = individual.genome;
        neuralNetwork.setWeights(weights);
        individual.fitness = fitness;
        individual.info = printFitness;

        scanner.close();
        return individual;
    }
}
