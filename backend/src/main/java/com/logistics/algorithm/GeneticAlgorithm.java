// algorithm/GeneticAlgorithm.java
package com.logistics.algorithm;

import java.util.*;

/**
 * 改进遗传算法解决车辆路径问题（VRP）
 */
public class GeneticAlgorithm {

    // 算法参数
    private int populationSize = 100;      // 种群大小
    private int maxGeneration = 200;       // 最大迭代次数
    private int maxNoImprovement = 30;     // 无改进的最大代数（早停机制）
    private double crossoverRate = 0.85;   // 交叉概率
    private double mutationRate = 0.1;     // 变异概率
    private double eliteRate = 0.1;        // 精英保留比例

    // 问题数据
    private double[][] distanceMatrix;     // 距离矩阵
    private double vehicleCapacity;        // 车辆载重
    private List<Double> demands;          // 各点需求量

    /**
     * 位置类
     */
    public static class Location {
        public double lng;
        public double lat;
        public Location(double lng, double lat) {
            this.lng = lng;
            this.lat = lat;
        }
    }

    /**
     * 解决方案类
     */
    public static class Solution {
        public List<List<Integer>> routes;   // 多条路径
        public double totalDistance;         // 总距离
        public double fitness;               // 适应度

        public Solution() {
            this.routes = new ArrayList<>();
            this.totalDistance = 0;
            this.fitness = 0;
        }
    }

    public GeneticAlgorithm(double[][] distanceMatrix, List<Double> demands, double vehicleCapacity) {
        this.distanceMatrix = distanceMatrix;
        this.demands = demands;
        this.vehicleCapacity = vehicleCapacity;
    }

    /**
     * 初始化种群
     */
    private List<List<Integer>> initializePopulation() {
        List<List<Integer>> population = new ArrayList<>();
        int customerCount = distanceMatrix.length - 1;
        List<Integer> customers = new ArrayList<>();
        for (int i = 1; i <= customerCount; i++) {
            customers.add(i);
        }

        int heuristicCount = Math.min(populationSize / 5, 10);
        for (int i = 0; i < heuristicCount; i++) {
            int startCustomer = (i % customerCount) + 1;
            List<Integer> individual = nearestNeighborHeuristic(startCustomer);
            if (individual != null) {
                population.add(new ArrayList<>(individual));
            }
        }

        while (population.size() < populationSize) {
            List<Integer> individual = new ArrayList<>(customers);
            Collections.shuffle(individual);
            population.add(individual);
        }
        return population;
    }

    /**
     * 最近邻启发式算法
     */
    private List<Integer> nearestNeighborHeuristic(int startCustomer) {
        int customerCount = distanceMatrix.length - 1;
        List<Integer> route = new ArrayList<>();
        boolean[] visited = new boolean[customerCount + 1];
        int current = startCustomer;
        visited[current] = true;
        route.add(current);

        while (route.size() < customerCount) {
            int nextCustomer = -1;
            double minDistance = Double.MAX_VALUE;
            for (int i = 1; i <= customerCount; i++) {
                if (!visited[i] && distanceMatrix[current][i] < minDistance) {
                    minDistance = distanceMatrix[current][i];
                    nextCustomer = i;
                }
            }
            if (nextCustomer == -1) {
                break;
            }
            route.add(nextCustomer);
            visited[nextCustomer] = true;
            current = nextCustomer;
        }
        return route.size() == customerCount ? route : null;
    }

    /**
     * 解码：将染色体解码为路径（考虑载重约束）
     */
    private Solution decode(List<Integer> chromosome) {
        Solution solution = new Solution();
        List<Integer> currentRoute = new ArrayList<>();
        currentRoute.add(0);  // 从仓库出发
        double currentLoad = 0;

        for (int customer : chromosome) {
            double demand = demands.get(customer);
            // 如果超载，则结束当前路径，开启新路径
            if (currentLoad + demand > vehicleCapacity && currentRoute.size() > 1) {
                currentRoute.add(0);  // 返回仓库
                solution.routes.add(new ArrayList<>(currentRoute));
                currentRoute.clear();
                currentRoute.add(0);  // 新路径从仓库开始
                currentLoad = 0;
            }
            currentRoute.add(customer);
            currentLoad += demand;
        }
        currentRoute.add(0);  // 最后返回仓库
        solution.routes.add(currentRoute);

        // 计算总距离
        double totalDist = 0;
        for (List<Integer> route : solution.routes) {
            for (int i = 0; i < route.size() - 1; i++) {
                totalDist += distanceMatrix[route.get(i)][route.get(i + 1)];
            }
        }
        solution.totalDistance = totalDist;
        solution.fitness = 1.0 / (totalDist + 1);

        return solution;
    }

    /**
     * 计算种群中所有个体的适应度
     */
    private List<Solution> evaluatePopulation(List<List<Integer>> population) {
        List<Solution> solutions = new ArrayList<>();
        for (List<Integer> individual : population) {
            solutions.add(decode(individual));
        }
        // 按适应度降序排序
        solutions.sort((a, b) -> Double.compare(b.fitness, a.fitness));
        return solutions;
    }

    /**
     * 轮盘赌选择
     */
    private List<Integer> select(List<Solution> solutions, List<List<Integer>> population) {
        double totalFitness = solutions.stream().mapToDouble(s -> s.fitness).sum();
        double random = Math.random() * totalFitness;
        double sum = 0;
        for (int i = 0; i < solutions.size(); i++) {
            sum += solutions.get(i).fitness;
            if (sum >= random) {
                return new ArrayList<>(population.get(i));
            }
        }
        return new ArrayList<>(population.get(0));
    }

    /**
     * 有序交叉（OX）
     */
    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        if (Math.random() > crossoverRate) {
            return new ArrayList<>(parent1);
        }

        int size = parent1.size();
        List<Integer> child = new ArrayList<>(Collections.nCopies(size, -1));

        // 随机选择交叉区间
        int start = (int)(Math.random() * size);
        int end = (int)(Math.random() * (size - start)) + start;

        // 复制父代1的区间到子代
        for (int i = start; i <= end; i++) {
            child.set(i, parent1.get(i));
        }

        // 从父代2填充剩余位置
        int index = 0;
        for (int i = 0; i < size; i++) {
            if (child.get(i) == -1) {
                while (child.contains(parent2.get(index))) {
                    index++;
                }
                child.set(i, parent2.get(index));
                index++;
            }
        }
        return child;
    }

    /**
     * 变异（交换两个位置）
     */
    private void mutate(List<Integer> chromosome) {
        if (Math.random() > mutationRate) return;

        int size = chromosome.size();
        int pos1 = (int)(Math.random() * size);
        int pos2 = (int)(Math.random() * size);

        Collections.swap(chromosome, pos1, pos2);
    }

    /**
     * 局部搜索：对路径进行优化
     */
    private void localSearch(List<Integer> chromosome) {
        // 简单的2-opt局部搜索
        int size = chromosome.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                // 交换两个位置
                Collections.swap(chromosome, i, j);
                // 计算交换后的适应度
                Solution currentSolution = decode(chromosome);
                // 交换回来
                Collections.swap(chromosome, i, j);
                Solution originalSolution = decode(chromosome);
                // 如果交换后更好，则保留交换
                if (currentSolution.fitness > originalSolution.fitness) {
                    Collections.swap(chromosome, i, j);
                }
            }
        }
    }

    /**
     * 运行遗传算法
     */
    public Solution run() {
        // 初始化种群
        List<List<Integer>> population = initializePopulation();
        Solution bestSolution = null;
        double bestFitness = -1;
        int noImprovementCount = 0;

        for (int generation = 0; generation < maxGeneration; generation++) {
            // 评估
            List<Solution> solutions = evaluatePopulation(population);

            // 更新最优解
            if (solutions.get(0).fitness > bestFitness) {
                bestFitness = solutions.get(0).fitness;
                bestSolution = solutions.get(0);
                noImprovementCount = 0;
            } else {
                noImprovementCount++;
            }

            // 早停机制
            if (noImprovementCount >= maxNoImprovement) {
                break;
            }

            // 精英保留
            List<List<Integer>> newPopulation = new ArrayList<>();
            int eliteCount = (int)(populationSize * eliteRate);
            for (int i = 0; i < eliteCount; i++) {
                newPopulation.add(new ArrayList<>(population.get(i)));
            }

            // 生成下一代
            while (newPopulation.size() < populationSize) {
                List<Integer> parent1 = select(solutions, population);
                List<Integer> parent2 = select(solutions, population);
                List<Integer> child = crossover(parent1, parent2);
                mutate(child);
                // 对部分个体进行局部搜索，平衡搜索精度和速度
                if (Math.random() < 0.2) {
                    localSearch(child);
                }
                newPopulation.add(child);
            }

            population = newPopulation;

            // 自适应调整参数
            adaptParameters(generation);
        }

        return bestSolution;
    }

    /**
     * 自适应调整参数
     */
    private void adaptParameters(int generation) {
        // 随着迭代次数增加，降低变异率
        double progress = (double) generation / maxGeneration;
        mutationRate = 0.15 * (1 - progress * 0.6);
        crossoverRate = 0.9 - progress * 0.1;
    }
}