import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRBEnv;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBQuadExpr;
import gurobi.GRBVar;

public class CaminhoMinimo {
	static final double EPSILON = 0.000001;

	public static void main(String[] args) throws Exception {
		String directory = System.getProperty("user.dir");

		runInstance(directory + "/instancias/instancia_id1_n30_p4_Q100.txt");
	}

	static void runInstance(String fileName) throws Exception {
		Scanner in = new Scanner(new File(fileName));

		int quantityNodes = in.nextInt();
		int quantityVehicles = in.nextInt();
		int maxCapacityVehicles = in.nextInt();

		List<Node> nodes = new ArrayList<>();
		List<Vehicle> vehicles = new ArrayList<>();

		System.out.println("**** quantityNumbers: " + quantityNodes + "****");
		System.out.println("**** quantityVehicles: " + quantityVehicles + "****");
		System.out.println("**** maxCapacityVehicles: " + maxCapacityVehicles + "****");

		for (int i = 0; i < quantityVehicles; i++) {
			vehicles.add(new Vehicle(i, maxCapacityVehicles, 0));
		}

		for (int j = 0; j < quantityNodes; j++) {
			int x = in.nextInt();
			int y = in.nextInt();
			int demand = in.nextInt();

			nodes.add(new Node(j, x, y, demand));
		}

		System.out.println("Foram criados " + nodes.size() + " instâncias de nós e " + vehicles.size()
				+ " instâncias de veículos \n");

		nodes.forEach(item -> {
			System.out.println("Nó " + item.getNumber() + " = x: " + item.getX() + ", y: " + item.getY() + ", demanda: "
					+ item.getDemand());
		});

		vehicles.forEach(item -> {
			System.out.println("Veículo " + item.getNumber() + " = maxCapacity: " + item.getMaxCapacity());
		});

		System.out.println("\n");

		resolveInstance(nodes, vehicles);
	}

	static void resolveInstance(List<Node> nodes, List<Vehicle> vehicles) throws Exception {
		/*
		 * for (int i = 0; i < nos.size(); i++) { Node node = nos.get(i);
		 * 
		 * for (int j = 0; j < nos.size(); j++) { Node secondNode = nos.get(j);
		 * 
		 * System.out.println("Distancia entre no " + i + " e no " + j + ": " +
		 * getDistance(node.getX(), node.getY(), secondNode.getX(), secondNode.getY()));
		 * } }
		 */

		// criando um model "vazio" no gurobi
		GRBEnv env = new GRBEnv("dieta.log");
		GRBModel model = new GRBModel(env);

		System.out.println("Configurando modelo \n");

		Map<String, GRBVar> x = new HashMap<String, GRBVar>();
		Map<Integer, GRBVar> u = new HashMap<Integer, GRBVar>();
		// List<GRBVar> costs = new ArrayList<>();

		for (int v = 0; v < vehicles.size(); v++) {
			Vehicle vehicle = vehicles.get(v);

			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);

				for (int j = 0; j < nodes.size(); j++) {
					Node secondNode = nodes.get(j);

					if (node.getNumber() != secondNode.getNumber()) {
						x.put(getKey(vehicle, node, secondNode), model.addVar(0.0, 1.0, getDistance(node, secondNode),
								GRB.BINARY, "x= " + getKey(vehicle, node, secondNode)));
					}

					// costs.add(model.addVar(0.0, GRB.INFINITY, getDistance(node, secondNode),
					// GRB.CONTINUOUS,
					// "distancia origem: " + node.getNumber() + "destino: " +
					// secondNode.getNumber()));
				}
			}
		}

		for (int i = 1; i < nodes.size(); i++) {
			Node node = nodes.get(i);

			// testar passando a demanda
			u.put(node.getNumber(),
					model.addVar(node.getDemand(), vehicles.get(0).getMaxCapacity(), 0, GRB.INTEGER, "u= " + node.getNumber()));
		}

		// função objetivo de minimização
		model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
		model.set(GRB.DoubleParam.TimeLimit, 3600.0);

		System.out.println("Restrição 1 \n");
		// Restrição 1
		for (Vehicle vehicle : vehicles) {
			for (Node node : nodes) {
				GRBLinExpr expr = new GRBLinExpr();

				for (Node destinationNode : nodes) {
					if (destinationNode.getNumber() != node.getNumber()) {
						expr.addTerm(1, x.get(getKey(vehicle, node, destinationNode)));
					}
				}

				for (Node originNode : nodes) {
					if (originNode.getNumber() != node.getNumber()) {
						expr.addTerm(-1, x.get(getKey(vehicle, originNode, node)));
					}
				}

				model.addConstr(expr, GRB.EQUAL, 0, "RES 1, NÓ " + node.getNumber());
			}
		}

		System.out.println("Restrição 2 \n");
		// Restrição 2
		for (int i = 1; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			GRBLinExpr expr = new GRBLinExpr();

			for (Vehicle vehicle : vehicles) {
				for (Node originNode : nodes) {
					if (originNode.getNumber() != node.getNumber()) {
						expr.addTerm(1, x.get(getKey(vehicle, originNode, node)));
					}
				}
			}

			model.addConstr(expr, GRB.EQUAL, 1, "RES 2, NÓ " + node.getNumber());
		}

		System.out.println("Restrição 3 \n");
		for (Vehicle vehicle : vehicles) {
			GRBLinExpr expr = new GRBLinExpr();

			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);

				for (int j = 0; j < nodes.size(); j++) {
					Node secondNode = nodes.get(j);

					if (node.getNumber() != secondNode.getNumber()) {
						expr.addTerm(secondNode.getDemand(), x.get(getKey(vehicle, node, secondNode)));
					}
				}
			}

			model.addConstr(expr, GRB.LESS_EQUAL, vehicle.getMaxCapacity(), "RES 3, CARRO " + vehicle.getNumber());
		}
		
		System.out.println("Restrição 4 \n");
		for (int i = 1; i < nodes.size(); i++) {
			Node originNode = nodes.get(i);
			
			for (int j = 1; j < nodes.size(); j++) {
				Node destinationNode = nodes.get(j);
				
				if (originNode.getNumber() != destinationNode.getNumber()) {
					GRBLinExpr leftExpr = new GRBLinExpr();
					GRBLinExpr rightExpr = new GRBLinExpr();
					
					leftExpr.addTerm(1, u.get(destinationNode.getNumber()));
					leftExpr.addTerm(-1, u.get(originNode.getNumber()));
					
					GRBLinExpr vehiclesExpr = new GRBLinExpr();
					
					vehiclesExpr.addConstant(1);
					
					rightExpr.addConstant(destinationNode.getDemand());
					rightExpr.addConstant(vehicles.get(0).getMaxCapacity() * -1);

					for(Vehicle vehicle : vehicles) {
						rightExpr.addTerm(vehicles.get(0).getMaxCapacity(), x.get(getKey(vehicle, originNode, destinationNode)));
					}
					
					//rightExpr.multAdd(vehicles.get(0).getMaxCapacity(), vehiclesExpr);
					
					model.addConstr(leftExpr, GRB.GREATER_EQUAL, rightExpr, null);
				}
			}
		}

		// Restrição deve sair do deposito!
		for (Vehicle vehicle : vehicles) {
			GRBLinExpr expr = new GRBLinExpr();
			Node deposit = nodes.get(0);

			for (Node node : nodes) {
				if (deposit.getNumber() != node.getNumber()) {
					expr.addTerm(1, x.get(getKey(vehicle, deposit, node)));
				}
			}

			model.addConstr(expr, GRB.EQUAL, 1, "Rest saida deposito veiculo " + vehicle.getNumber());
		}

		// Restrição deve entrar no deposito!
		for (Vehicle vehicle : vehicles) {
			GRBLinExpr expr = new GRBLinExpr();
			Node deposit = nodes.get(0);

			for (Node node : nodes) {
				if (deposit.getNumber() != node.getNumber()) {
					expr.addTerm(-1, x.get(getKey(vehicle, node, deposit)));
				}
			}

			model.addConstr(expr, GRB.EQUAL, -1, "Rest volta deposito veiculo " + vehicle.getNumber());
		}

		System.out.println("Chamando resolução do solver \n");
		// chama o solver para resolver o modelo
		
		model.write(System.getProperty("user.dir") + "modelo.lp");
		model.optimize();
		
		/*

		// deu tudo certo?
		if (model.get(IntAttr.Status) != GRB.OPTIMAL) {
			throw new RuntimeException("Status: " + IntAttr.Status);
		}
		*/

		System.out.println("Resolvido! Resultados: \n");
		for (Map.Entry<String, GRBVar> varX : x.entrySet()) {
			if (varX.getValue().get(DoubleAttr.X) > EPSILON) {
				System.out.println(varX.getKey() + ": " + varX.getValue().get(DoubleAttr.X));
			}
		}
		
		for (Map.Entry<Integer, GRBVar> varU : u.entrySet()) {
			//if (varU.getValue().get(DoubleAttr.X) > EPSILON) {
				System.out.println(varU.getKey() + ": " + varU.getValue().get(DoubleAttr.X));
			//}
		}
	}

	public static String getKey(Vehicle vehicle, Node no1, Node no2) {
		return "veículo: " + vehicle.getNumber() + ", origem: " + no1.getNumber() + ", destino: " + no2.getNumber();
	}

	public static double getDistance(Node node1, Node node2) {
		double a = node2.getX() - node1.getX();
		double b = node2.getY() - node1.getY();
		double c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return c;
	}
}
