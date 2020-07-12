package AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 *
 * @author Antonis Gkortzis (s2583070, antonis.gkortzis@gmail.com)
 */
public class ClassVisitor {

	private CompilationUnit cu = null;
	private final String sourcecode;
	private static ArrayList<String> output;

	public ClassVisitor(String sourcecode) {
		this.sourcecode = sourcecode;
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(sourcecode.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		cu = (CompilationUnit) parser.createAST(null);
		output = new ArrayList<String>();
	}

	public ArrayList<String> getOutput() {
		return output;
	}

	public void visit(final ArrayList<ArrayList<Integer>> invalid_lines) {

		cu.accept(new ASTVisitor() {

			Set<String> defined_variables = new HashSet<String>();
			ArrayList<Integer> not_counted_lines = new ArrayList<Integer>();
			HashMap<Integer, ArrayList<String>> not_used_due_to_invocation = new HashMap<Integer, ArrayList<String>>();
			HashMap<Integer[], String> switchStatements = new HashMap<Integer[], String>();
			HashMap<Integer[], ArrayList<String>> ifStatements = new HashMap<Integer[], ArrayList<String>>();
			ArrayList<Integer> elseIfLines = new ArrayList<Integer>();

			public void printToFile(String type, String name, int line) {

				boolean found = false;
				int j = 0;

				if (name.startsWith("java.") || name.startsWith("Collections") || name.startsWith("utils.")
						|| name.startsWith("Iterable") || name.startsWith("System.") || name.startsWith("Map.")
						|| name.startsWith("Arrays") || name.startsWith("ArrayList") || name.startsWith("HashSet")
						|| name.startsWith("HashMap") || name.startsWith("File") || name.startsWith("HashEntry")
						|| name.startsWith("EnumMap") || name.startsWith("Utils.") || name.contains("xception")) {
					// not printed
				} else {
					for (int i = 0; i < invalid_lines.size(); i++) {
						ArrayList<Integer> invalid_lines_cluster = invalid_lines.get(i);
						if (invalid_lines_cluster.contains(line)) {
							found = true;
							for (j = 0; j < invalid_lines_cluster.size(); j++) {
								if (j == 0) {
									if (type.equals("Invocation")) {
										if (name.contains(".")) {
											String[] temp = name.split("\\.");
											if (validAsUsage(temp[0])) {
												// if (temp[0].contains("++")) temp[0] = temp[0].replace("++", "");
												// if (temp[0].contains("--")) temp[0] = temp[0].replace("--", "");
												if (temp[0].contains(" ")) {
													temp[0] = temp[0].replaceAll(" ", "");
												}
												if (temp[0].contains("[")) {
													temp[0] = temp[0].replaceAll("\\[.*", "");
												}
												if (temp[0].contains("-")) {
													temp[0] = temp[0].replaceAll("-", "");
												}
												if (temp[0].contains("+")) {
													temp[0] = temp[0].replaceAll("+", "");
												}
												if (temp[0].contains("=")) {
													temp[0] = temp[0].replaceAll("=", "");
												}
												if (cleanUpMethodName(temp[0], null).equals("1") == false
														&& cleanUpMethodName(temp[0], null).equals("-1") == false) {
													String out = "Usage#" + cleanUpMethodName(temp[0], null) + "#"
															+ invalid_lines_cluster.get(j) + ";";
//                                                    System.out.println(out);//original
													output.add(out);
												}
											}
										}
									}
									if (type.equals("Invocation-IF")) {
										if (name.contains(".")) {
											String[] temp = name.split("\\.");
											if (validAsUsage(temp[0])) {
												if (temp[0].contains(" ")) {
													temp[0] = temp[0].replaceAll(" ", "");
												}
												if (temp[0].contains("[")) {
													temp[0] = temp[0].replaceAll("\\[.*", "");
												}
												if (temp[0].contains("-")) {
													temp[0] = temp[0].replaceAll("-", "");
												}
												if (temp[0].contains("+")) {
													temp[0] = temp[0].replaceAll("+", "");
												}
												if (temp[0].contains("=")) {
													temp[0] = temp[0].replaceAll("=", "");
												}
												if (cleanUpMethodName(temp[0], null).equals("1") == false
														&& cleanUpMethodName(temp[0], null).equals("-1") == false) {
													String out = "Usage-IF#" + cleanUpMethodName(temp[0], null) + "#"
															+ invalid_lines_cluster.get(j) + ";";
//                                                    System.out.println(out);//original
													output.add(out);
												}
											}
										}
									}
									if (name.contains(" ")) {
										name = name.replaceAll(" ", "");
									}
									if (name.contains("=")) {
										name = name.replaceAll("=", "");
									}
									if (name.contains("+")) {
										name = name.replaceAll("+", "");
									}
									if (name.contains("-")) {
										name = name.replaceAll("-", "");
									}
									if (name.contains("[")) {
										name = name.replaceAll("\\[.*", "");
									}
									if (cleanUpMethodName(name, null).equals("1") == false
											&& cleanUpMethodName(name, null).equals("-1") == false) {
										String out = type + "#" + cleanUpMethodName(name, null) + "#"
												+ invalid_lines_cluster.get(j) + ";";
//                                        System.out.println(out);//original
										output.add(out);
									}
								} else if (type.equals("Declaration")) {
									if (validAsUsage(name)) {
										if (name.contains(" ")) {
											name = name.replaceAll(" ", "");
										}
										if (name.contains("=")) {
											name = name.replaceAll("=", "");
										}
										if (name.contains("+")) {
											name = name.replaceAll("+", "");
										}
										if (name.contains("-")) {
											name = name.replaceAll("-", "");
										}
										if (name.contains("[")) {
											name = name.replaceAll("\\[.*", "");
										}
										if (cleanUpMethodName(name, null).equals("1") == false
												&& cleanUpMethodName(name, null).equals("-1") == false) {
											String out = "Usage#" + cleanUpMethodName(name, null) + "#"
													+ invalid_lines_cluster.get(j) + ";";
//                                                System.out.println(out);//original
											output.add(out);
										}
									}
								} else if (type.equals("Invocation")) {
									if (name.contains(".")) {
										String[] temp = name.split("\\.");
										if (validAsUsage(temp[0])) {
											if (temp[0].contains("[")) {
												temp[0] = temp[0].replaceAll("\\[.*", "");
											}
											if (temp[0].contains("-")) {
												temp[0] = temp[0].replaceAll("-", "");
											}
											if (temp[0].contains("+")) {
												temp[0] = temp[0].replaceAll("+", "");
											}
											if (temp[0].contains("=")) {
												temp[0] = temp[0].replaceAll("=", "");
											}
											if (temp[0].contains(" ")) {
												temp[0] = temp[0].replaceAll(" ", "");
											}
											if (cleanUpMethodName(temp[0], null).equals("1") == false
													&& cleanUpMethodName(temp[0], null).equals("-1") == false) {
												String out = "Usage#" + cleanUpMethodName(temp[0], null) + "#"
														+ invalid_lines_cluster.get(j) + ";";
//                                                    System.out.println(out);//original
												output.add(out);
											}
										}
									}
									if (name.contains(" ")) {
										name = name.replaceAll(" ", "");
									}
									if (name.contains("=")) {
										name = name.replaceAll("=", "");
									}
									if (name.contains("+")) {
										name = name.replaceAll("+", "");
									}
									if (name.contains("-")) {
										name = name.replaceAll("-", "");
									}
									if (name.contains("[")) {
										name = name.replaceAll("\\[.*", "");
									}
									if (cleanUpMethodName(name, null).equals("1") == false
											&& cleanUpMethodName(name, null).equals("-1") == false) {
										String out = type + "#" + cleanUpMethodName(name, null) + "#"
												+ invalid_lines_cluster.get(j) + ";";
//                                            System.out.println(out);//original
										output.add(out);
									}
								} else if (type.equals("Invocation-IF")) {
									if (name.contains(".")) {
										String[] temp = name.split("\\.");
										if (validAsUsage(temp[0])) {
											if (temp[0].contains("[")) {
												temp[0] = temp[0].replaceAll("\\[.*", "");
											}
											if (temp[0].contains("-")) {
												temp[0] = temp[0].replaceAll("-", "");
											}
											if (temp[0].contains("+")) {
												temp[0] = temp[0].replaceAll("+", "");
											}
											if (temp[0].contains("=")) {
												temp[0] = temp[0].replaceAll("=", "");
											}
											if (temp[0].contains(" ")) {
												temp[0] = temp[0].replaceAll(" ", "");
											}
											if (cleanUpMethodName(temp[0], null).equals("1") == false
													&& cleanUpMethodName(temp[0], null).equals("-1") == false) {
												String out = "Usage-IF#" + cleanUpMethodName(temp[0], null) + "#"
														+ invalid_lines_cluster.get(j) + ";";
//                                                    System.out.println(out);//original
												output.add(out);
											}
										}
									}
									if (name.contains(" ")) {
										name = name.replaceAll(" ", "");
									}
									if (name.contains("=")) {
										name = name.replaceAll("=", "");
									}
									if (name.contains("+")) {
										name = name.replaceAll("+", "");
									}
									if (name.contains("-")) {
										name = name.replaceAll("-", "");
									}
									if (name.contains("[")) {
										name = name.replaceAll("\\[.*", "");
									}
									if (cleanUpMethodName(name, null).equals("1") == false
											&& cleanUpMethodName(name, null).equals("-1") == false) {
										String out = type + "#" + cleanUpMethodName(name, null) + "#"
												+ invalid_lines_cluster.get(j) + ";";
//                                            System.out.println(out);//original
										output.add(out);
									}
								} else {
									if (name.contains(" ")) {
										name = name.replaceAll(" ", "");
									}
									if (name.contains("=")) {
										name = name.replaceAll("=", "");
									}
									if (name.contains("+")) {
										name = name.replaceAll("+", "");
									}
									if (name.contains("-")) {
										name = name.replaceAll("-", "");
									}
									if (name.contains("[")) {
										name = name.replaceAll("\\[.*", "");
									}
									if (cleanUpMethodName(name, null).equals("1") == false
											&& cleanUpMethodName(name, null).equals("-1") == false) {
										String out = type + "#" + cleanUpMethodName(name, null) + "#"
												+ invalid_lines_cluster.get(j) + ";";
//                                            System.out.println(out);//original
										output.add(out);
									}
								}
							}
							break;
						}
					}
					if (found == false) {
						if (type.equals("Invocation")) {
							if (name.contains(".")) {
								String[] temp = name.split("\\.");
								if (temp[0].contains("[")) {
									temp[0] = temp[0].replaceAll("\\[.*", "");
								}
								if (temp[0].contains("-")) {
									temp[0] = temp[0].replaceAll("-", "");
								}
								if (temp[0].contains("+")) {
									temp[0] = temp[0].replaceAll("+", "");
								}
								if (temp[0].contains("=")) {
									temp[0] = temp[0].replaceAll("=", "");
								}
								if (temp[0].contains(" ")) {
									temp[0] = temp[0].replaceAll(" ", "");
								}
								if (name.contains("constructor") == false) {
									if (cleanUpMethodName(temp[0], null).equals("1") == false
											&& cleanUpMethodName(temp[0], null).equals("-1") == false) {
										String out = "Usage#" + cleanUpMethodName(temp[0], null) + "#" + line + ";";
//                                        System.out.println(out);//original
										output.add(out);
									}
								}
							}
						}
						if (name.contains(" ")) {
							name = name.replaceAll(" ", "");
						}
						if (name.contains("=")) {
							name = name.replaceAll("=", "");
						}
						if (name.contains("+")) {
							name = name.replaceAll("+", "");
						}
						if (name.contains("-")) {
							name = name.replaceAll("-", "");
						}
						if (name.contains("[")) {
							name = name.replaceAll("\\[.*", "");
						}
						if (cleanUpMethodName(name, null).equals("1") == false
								&& cleanUpMethodName(name, null).equals("-1") == false) {
							String out = type + "#" + cleanUpMethodName(name, null) + "#" + line + ";";
//                            System.out.println(out);//original
							output.add(out);
						}
					}
				}
			}

			public boolean isInteger(String s) {
				try {
					Integer.parseInt(s);
				} catch (NumberFormatException e) {
					return false;
				}
				// only got here if we didn't return false
				return true;
			}

			public boolean isFloat(String s) {
				try {
					Float.parseFloat(s);
				} catch (NumberFormatException e) {
					return false;
				}
				// only got here if we didn't return false
				return true;
			}

			public boolean validAsUsage(String s1) {

				// System.out.println("----------------------------------------Checking:" + s1);
				boolean temp = false;
				if (isInteger(s1) == false) {
					if (s1.equals("null") == false && s1.equals("true") == false && s1.equals("false") == false) {
						if (s1.contains("\"") == false) {
							if (s1.contains("(") == false) {
								if (s1.contains(":") == false) {
									if (s1.length() > 0) {
										if (s1.contains("+") == false) {
											if (s1.contains("-") == false) {
												if (s1.contains("*") == false) {
													if (s1.contains("/") == false) {
														if (s1.contains("<") == false) {
															if (s1.contains("&&") == false) {
																if (s1.contains("||") == false) {
																	if (s1.contains("=") == false) {
																		if (s1.contains("'") == false) {
																			if (isFloat(s1) == false) {
																				temp = true;
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				return temp;
			}

			public void handleConditionalExpressions(Expression e) {

				if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
					printArguments((MethodInvocation) e);
					String methodCall = e.toString();
					addParametersAsUsage(methodCall, e);
					methodCall = cleanUpMethodName(methodCall, (MethodInvocation) e);
					if (!methodCall.contains("new ")) {
						printToFile("Invocation", methodCall, cu.getLineNumber(e.getStartPosition()));
					}
					// System.out.println("Invocation#" + methodCall + "#" +
					// cu.getLineNumber(e.getStartPosition())+ ";");
				} else if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.CastExpression")) {
					CastExpression ce = (CastExpression) e;
					if (ce.getExpression().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						printArguments((MethodInvocation) ce.getExpression());
						printToFile("Invocation",
								cleanUpMethodName(ce.getExpression().toString(), (MethodInvocation) ce.getExpression()),
								cu.getLineNumber(e.getStartPosition()));
						// System.out.println("Invocation#" +
						// cleanUpMethodName(ce.getExpression().toString()) + "#" +
						// cu.getLineNumber(node.getStartPosition())+ ";");
					} else {
						printToFile("Usage", ce.getExpression().toString(), cu.getLineNumber(e.getStartPosition()));
						// System.out.println("Usage#" + ce.getExpression() + "#" +
						// cu.getLineNumber(node.getStartPosition())+ ";");
					}
				} else if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
					ParenthesizedExpression p = (ParenthesizedExpression) e;
					e = p.getExpression();
					if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						printArguments((MethodInvocation) e);
						String methodCall = e.toString();
						addParametersAsUsage(methodCall, e);
						methodCall = cleanUpMethodName(methodCall, (MethodInvocation) e);
						if (!methodCall.contains("new ")) {
							printToFile("Invocation", methodCall, cu.getLineNumber(e.getStartPosition()));
						}
						// System.out.println("Invocation#" + methodCall + "#" +
						// cu.getLineNumber(e.getStartPosition())+ ";");
					} else if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						ArrayList<String> operands = getAllLeftOperandsInExpression((InfixExpression) e);
						for (int q = 0; q < operands.size(); q++) {
							if (operands.get(q).contains("(")) {
								printToFile("Invocation", cleanUpMethodName(operands.get(q), null),
										cu.getLineNumber(e.getStartPosition()));
							} else {
								printToFile("Usage", operands.get(q), cu.getLineNumber(e.getStartPosition()));
							}
						}
					} else {
						validAsUsage(e.toString());
					}
				} else if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
					ArrayList<String> operands = getAllLeftOperandsInExpression((InfixExpression) e);
					for (int q = 0; q < operands.size(); q++) {
						if (operands.get(q).contains("(")) {
							printToFile("Invocation", cleanUpMethodName(operands.get(q), null),
									cu.getLineNumber(e.getStartPosition()));
						} else {
							printToFile("Usage", operands.get(q), cu.getLineNumber(e.getStartPosition()));
						}
					}
				} else {
					validAsUsage(e.toString());
				}
			}

			public void addParametersAsUsage(String methodCall, ASTNode node) {

				int param_start = methodCall.indexOf('(');
				int param_end = methodCall.lastIndexOf(')');
				

				//if(param_start==-1 || param_end==-1) return;	//***DEBUG
				if(param_end==-1) return;	//***DEBUG
				
				//System.out.println("Neo methodCall!");
				//System.out.println("Neo param_start= " + param_start);
				//System.out.println("Neo param_end= " + param_end);
				//System.out.println("***DEBUG String =" + methodCall);		
				
				
				String params = methodCall.substring(param_start + 1, param_end);
				if (params.indexOf(')') == 0) {
					// do nothing
				} else {
					String[] temp = params.split(",");
					for (int j = 0; j < temp.length; j++) {
						if (validAsUsage(temp[j])) {
							printToFile("Usage", temp[j], cu.getLineNumber(node.getStartPosition()));

						}
					}
				}

			}

			public int firstNotPrimitiveExpressions(ArrayList<InfixExpression> allExpressions) {
				boolean allPrimitive = true;
				int i;
				for (i = 0; i < allExpressions.size(); i++) {
					// if (allExpressions.get(i).getOperator().toString().equals("&&") ||
					// allExpressions.get(i).getOperator().toString().equals("||") ||
					// allExpressions.get(i).getOperator().toString().equals("+") ||
					// allExpressions.get(i).getOperator().toString().equals("-") ||
					// allExpressions.get(i).getOperator().toString().equals("/") ||
					// allExpressions.get(i).getOperator().toString().equals("*")) {
					if (allExpressions.get(i).getOperator().toString().equals("&&")
							|| allExpressions.get(i).getOperator().toString().equals("||")) {
						allPrimitive = false;
						break;
					}
					// String temp = allExpressions.get(i).getLeftOperand().getClass().toString();
					if (allExpressions.get(i).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")
							|| allExpressions.get(i).getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
						allPrimitive = false;
						break;
					}

					if (allExpressions.get(i).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")
							|| allExpressions.get(i).getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						allPrimitive = false;
						break;
					}

					if (allExpressions.get(i).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InfixExpression")
							|| allExpressions.get(i).getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						allPrimitive = false;
						break;
					}

					if (allExpressions.get(i).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.CastExpression")
							|| allExpressions.get(i).getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
						allPrimitive = false;
						break;
					}

				}
				if (allPrimitive) {
					i = -1;
				}
				return i;
			}

			public void printArguments(MethodInvocation mi) {

				for (int i = 0; i < mi.arguments().size(); i++) {

					Expression e;
					MethodInvocation mi1, mi2;
					String methodCall;
					if (mi.arguments().get(i).getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						mi2 = (MethodInvocation) mi.arguments().get(i);
						printToFile("Invocation", cleanUpMethodName(mi2.toString(), mi2),
								cu.getLineNumber(mi.getStartPosition()));
						printArguments(mi2);
						if (mi2.getExpression() != null) {
							e = mi2.getExpression();
							if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								mi1 = (MethodInvocation) e;
								printArguments(mi1);
								methodCall = cleanUpMethodName(mi1.toString(), mi1);
								printToFile("Invocation", methodCall, cu.getLineNumber(mi1.getStartPosition()));
								if (mi1.getExpression() != null) {
									e = mi1.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = cleanUpMethodName(mi1.toString(), mi1);
										printToFile("Invocation", methodCall, cu.getLineNumber(mi1.getStartPosition()));
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = cleanUpMethodName(mi1.toString(), mi1);
												printToFile("Invocation", methodCall,
														cu.getLineNumber(mi1.getStartPosition()));
											}
										}
									}
								}
							}
						}
						methodCall = cleanUpMethodName(mi.toString(), mi);
						printToFile("Invocation", methodCall, cu.getLineNumber(mi.getStartPosition()));
					}

					if (mi.arguments().get(i).getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						InfixExpression e1 = (InfixExpression) mi.arguments().get(i);
						ArrayList<String> operands = getAllLeftOperandsInExpression(e1);
						for (int q = 0; q < operands.size(); q++) {
							if (operands.get(q).contains("(")) {
								printToFile("Invocation", cleanUpMethodName(operands.get(q), null),
										cu.getLineNumber(e1.getStartPosition()));
							} else if (validAsUsage(operands.get(q))) {
								printToFile("Usage", operands.get(q), cu.getLineNumber(e1.getStartPosition()));
							}
						}
					}

					if (mi.arguments().get(i).getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
						CastExpression ce = (CastExpression) mi.arguments().get(i);
						if (ce.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							printToFile("Invocation",
									cleanUpMethodName(ce.getExpression().toString(),
											(MethodInvocation) ce.getExpression()),
									cu.getLineNumber(ce.getStartPosition()));
						} else if (validAsUsage(ce.getExpression().toString())) {
							printToFile("Usage", ce.getExpression().toString(),
									cu.getLineNumber(ce.getStartPosition()));
						}
					}

					String argument = mi.arguments().get(i).toString();
					if (validAsUsage(argument)) {
						printToFile("Usage", argument, cu.getLineNumber(mi.getStartPosition()));
					}
				}
			}

			public ArrayList<String> getAllLeftOperandsInAssignmentExpression(InfixExpression infx) {

				Expression e;
				MethodInvocation mi1;
				String methodCall;
				ArrayList<InfixExpression> allExpressions = new ArrayList<InfixExpression>();
				ArrayList<String> temp = new ArrayList<String>();

				allExpressions.add(infx);
				// loop that searches all infix expressions and splits them
				while (firstNotPrimitiveExpressions(allExpressions) != -1) {
					int pos = firstNotPrimitiveExpressions(allExpressions);

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
						ConditionalExpression ie = (ConditionalExpression) allExpressions.get(pos).getLeftOperand();
						handleConditionalExpressions(ie.getElseExpression());
						handleConditionalExpressions(ie.getThenExpression());
						handleConditionalExpressions(ie.getExpression());
					}
					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
						ConditionalExpression ie = (ConditionalExpression) allExpressions.get(pos).getRightOperand();
						handleConditionalExpressions(ie.getElseExpression());
						handleConditionalExpressions(ie.getThenExpression());
						handleConditionalExpressions(ie.getExpression());
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
						if (validAsUsage(allExpressions.get(pos).getLeftOperand().toString())) {
							temp.add(allExpressions.get(pos).getLeftOperand().toString());
						}
					}

					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
						if (validAsUsage(allExpressions.get(pos).getRightOperand().toString())) {
							temp.add(allExpressions.get(pos).getRightOperand().toString());
						}
					}

					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
						CastExpression ce = (CastExpression) allExpressions.get(pos).getRightOperand();
						if (ce.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							printArguments((MethodInvocation) ce.getExpression());
							temp.add(ce.getExpression().toString());
						} else {
							temp.add(ce.getExpression().toString());
						}
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
						CastExpression ce = (CastExpression) allExpressions.get(pos).getLeftOperand();
						if (ce.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							printArguments((MethodInvocation) ce.getExpression());
							temp.add(ce.getExpression().toString());
						} else {
							temp.add(ce.getExpression().toString());
						}
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						allExpressions.add((InfixExpression) allExpressions.get(pos).getLeftOperand());
					}

					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						allExpressions.add((InfixExpression) allExpressions.get(pos).getRightOperand());
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
						InstanceofExpression ioe = (InstanceofExpression) allExpressions.get(pos).getLeftOperand();
						temp.add(ioe.getLeftOperand().toString());
					}

					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
						InstanceofExpression ioe = (InstanceofExpression) allExpressions.get(pos).getRightOperand();
						temp.add(ioe.getLeftOperand().toString());
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						MethodInvocation mi = (MethodInvocation) allExpressions.get(pos).getLeftOperand();
						methodCall = mi.toString();
						temp.add(methodCall);
						printArguments(mi);
						if (mi.getExpression() != null) {
							e = mi.getExpression();
							if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								mi1 = (MethodInvocation) e;
								printArguments(mi1);
								methodCall = mi1.toString();
								temp.add(methodCall);
								if (mi1.getExpression() != null) {
									e = mi1.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
											}
										}
									}
								}
							}
						}
						methodCall = mi.toString();
						temp.add(methodCall);
					}

					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						MethodInvocation mi = (MethodInvocation) allExpressions.get(pos).getRightOperand();
						printArguments(mi);
						methodCall = mi.toString();
						temp.add(methodCall);
						if (mi.getExpression() != null) {
							e = mi.getExpression();
							if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								mi1 = (MethodInvocation) e;
								printArguments(mi1);
								methodCall = mi1.toString();
								temp.add(methodCall);
								if (mi1.getExpression() != null) {
									e = mi1.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
											}
										}
									}
								}
							}
						}
						methodCall = mi.toString();
						temp.add(methodCall);
					}
					// special case of having a parenthesis as a left/right operand
					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
						ParenthesizedExpression pe = (ParenthesizedExpression) allExpressions.get(pos).getLeftOperand();

						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
							ConditionalExpression ie = (ConditionalExpression) pe.getExpression();
							handleConditionalExpressions(ie.getElseExpression());
							handleConditionalExpressions(ie.getThenExpression());
							handleConditionalExpressions(ie.getExpression());
						}

						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
								// allExpressions.add((InfixExpression)
								// allExpressions.get(pos).getLeftOperand());
								allExpressions.add((InfixExpression) ie.getLeftOperand());
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
								InstanceofExpression ioe = (InstanceofExpression) ie.getLeftOperand();
								temp.add(ioe.getLeftOperand().toString());
							}
							if (allExpressions.get(pos).getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								MethodInvocation mi = (MethodInvocation) ie.getLeftOperand();
								printArguments(mi);
								if (mi.getExpression() != null) {
									e = mi.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
												if (mi1.getExpression() != null) {
													e = mi1.getExpression();
													if (e.getClass().toString().equals(
															"class org.eclipse.jdt.core.dom.MethodInvocation")) {
														mi1 = (MethodInvocation) e;
														printArguments(mi1);
														methodCall = mi1.toString();
														temp.add(methodCall);
													}
												}
											}
										}
									}
								}
								methodCall = mi.toString();
								temp.add(methodCall);
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
								temp.add(ie.getLeftOperand().toString());
							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
								// allExpressions.add((InfixExpression)
								// allExpressions.get(pos).getRightOperand());
								allExpressions.add((InfixExpression) ie.getRightOperand());
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
								InstanceofExpression ioe = (InstanceofExpression) ie.getRightOperand();
								temp.add(ioe.getLeftOperand().toString());
							}
							if (allExpressions.get(pos).getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								MethodInvocation mi = (MethodInvocation) allExpressions.get(pos).getRightOperand();
								printArguments(mi);
								if (mi.getExpression() != null) {
									e = mi.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
												if (mi1.getExpression() != null) {
													e = mi1.getExpression();
													if (e.getClass().toString().equals(
															"class org.eclipse.jdt.core.dom.MethodInvocation")) {
														mi1 = (MethodInvocation) e;
														printArguments(mi1);
														methodCall = mi1.toString();
														temp.add(methodCall);
													}
												}
											}
										}
									}
								}
								methodCall = mi.toString();
								temp.add(methodCall);
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
								temp.add(ie.getRightOperand().toString());
							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
							InstanceofExpression ioe = (InstanceofExpression) pe.getExpression();
							temp.add(ioe.getLeftOperand().toString());
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
							InstanceofExpression ioe = (InstanceofExpression) pe.getExpression();
							temp.add(ioe.getRightOperand().toString());
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.Assignment")) {
							Assignment ass = (Assignment) pe.getExpression();
							temp.add(ass.getLeftHandSide().toString());
							if (validAsUsage(ass.getRightHandSide().toString())) {
								temp.add(ass.getRightHandSide().toString());

							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							MethodInvocation mi = (MethodInvocation) pe.getExpression();
							printArguments(mi);
							if (mi.getExpression() != null) {
								e = mi.getExpression();
								if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi1 = (MethodInvocation) e;
									printArguments(mi1);
									methodCall = mi1.toString();
									temp.add(methodCall);
									if (mi1.getExpression() != null) {
										e = mi1.getExpression();
										if (e.getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi1 = (MethodInvocation) e;
											printArguments(mi1);
											methodCall = mi1.toString();
											temp.add(methodCall);
											if (mi1.getExpression() != null) {
												e = mi1.getExpression();
												if (e.getClass().toString()
														.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
													mi1 = (MethodInvocation) e;
													printArguments(mi1);
													methodCall = mi1.toString();
													temp.add(methodCall);
												}
											}
										}
									}
								}
							}
							methodCall = mi.toString();
							temp.add(methodCall);
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							MethodInvocation mi = (MethodInvocation) pe.getExpression();
							printArguments(mi);
							if (mi.getExpression() != null) {
								e = mi.getExpression();
								if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi1 = (MethodInvocation) e;
									printArguments(mi1);
									methodCall = mi1.toString();
									temp.add(methodCall);
									if (mi1.getExpression() != null) {
										e = mi1.getExpression();
										if (e.getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi1 = (MethodInvocation) e;
											printArguments(mi1);
											methodCall = mi1.toString();
											temp.add(methodCall);
											if (mi1.getExpression() != null) {
												e = mi1.getExpression();
												if (e.getClass().toString()
														.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
													mi1 = (MethodInvocation) e;
													printArguments(mi1);
													methodCall = mi1.toString();
													temp.add(methodCall);
												}
											}
										}
									}
								}
							}
							methodCall = mi.toString();
							temp.add(methodCall);
						}
					}
					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
						ParenthesizedExpression pe = (ParenthesizedExpression) allExpressions.get(pos)
								.getRightOperand();

						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
							ConditionalExpression ie = (ConditionalExpression) pe.getExpression();
							handleConditionalExpressions(ie.getElseExpression());
							handleConditionalExpressions(ie.getThenExpression());
							handleConditionalExpressions(ie.getExpression());
						}

						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
								// allExpressions.add((InfixExpression)
								// allExpressions.get(pos).getLeftOperand());
								allExpressions.add((InfixExpression) ie.getLeftOperand());
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
								InstanceofExpression ioe = (InstanceofExpression) ie.getLeftOperand();
								temp.add(ioe.getLeftOperand().toString());
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								MethodInvocation mi = (MethodInvocation) ie.getLeftOperand();
								printArguments(mi);
								if (mi.getExpression() != null) {
									e = mi.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
												if (mi1.getExpression() != null) {
													e = mi1.getExpression();
													if (e.getClass().toString().equals(
															"class org.eclipse.jdt.core.dom.MethodInvocation")) {
														mi1 = (MethodInvocation) e;
														printArguments(mi1);
														methodCall = mi1.toString();
														temp.add(methodCall);
													}
												}
											}
										}
									}
								}
								methodCall = mi.toString();
								temp.add(methodCall);
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
								temp.add(ie.getLeftOperand().toString());
							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
								// allExpressions.add((InfixExpression)
								// allExpressions.get(pos).getRightOperand());
								allExpressions.add((InfixExpression) ie.getRightOperand());
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
								InstanceofExpression ioe = (InstanceofExpression) ie.getRightOperand();
								temp.add(ioe.getLeftOperand().toString());
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								MethodInvocation mi = (MethodInvocation) ie.getRightOperand();
								printArguments(mi);
								if (mi.getExpression() != null) {
									e = mi.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
												if (mi1.getExpression() != null) {
													e = mi1.getExpression();
													if (e.getClass().toString().equals(
															"class org.eclipse.jdt.core.dom.MethodInvocation")) {
														mi1 = (MethodInvocation) e;
														printArguments(mi1);
														methodCall = mi1.toString();
														temp.add(methodCall);
													}
												}
											}
										}
									}
								}
								methodCall = mi.toString();
								temp.add(methodCall);
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
								temp.add(ie.getRightOperand().toString());
							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
							InstanceofExpression ioe = (InstanceofExpression) pe.getExpression();
							temp.add(ioe.getLeftOperand().toString());
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
							InstanceofExpression ioe = (InstanceofExpression) pe.getExpression();
							temp.add(ioe.getRightOperand().toString());
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.Assignment")) {
							Assignment ass = (Assignment) pe.getExpression();
							temp.add(ass.getLeftHandSide().toString());
							if (validAsUsage(ass.getRightHandSide().toString())) {
								temp.add(ass.getRightHandSide().toString());

							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							MethodInvocation mi = (MethodInvocation) pe.getExpression();
							printArguments(mi);
							if (mi.getExpression() != null) {
								e = mi.getExpression();
								if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi1 = (MethodInvocation) e;
									printArguments(mi1);
									methodCall = mi1.toString();
									temp.add(methodCall);
									if (mi1.getExpression() != null) {
										e = mi1.getExpression();
										if (e.getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi1 = (MethodInvocation) e;
											printArguments(mi1);
											methodCall = mi1.toString();
											temp.add(methodCall);
											if (mi1.getExpression() != null) {
												e = mi1.getExpression();
												if (e.getClass().toString()
														.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
													mi1 = (MethodInvocation) e;
													printArguments(mi1);
													methodCall = mi1.toString();
													temp.add(methodCall);
												}
											}
										}
									}
								}
							}
							methodCall = mi.toString();
							temp.add(methodCall);
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							MethodInvocation mi = (MethodInvocation) pe.getExpression();
							printArguments(mi);
							if (mi.getExpression() != null) {
								e = mi.getExpression();
								if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi1 = (MethodInvocation) e;
									printArguments(mi1);
									methodCall = mi1.toString();
									temp.add(methodCall);
									if (mi1.getExpression() != null) {
										e = mi1.getExpression();
										if (e.getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi1 = (MethodInvocation) e;
											printArguments(mi1);
											methodCall = mi1.toString();
											temp.add(methodCall);
											if (mi1.getExpression() != null) {
												e = mi1.getExpression();
												if (e.getClass().toString()
														.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
													mi1 = (MethodInvocation) e;
													printArguments(mi1);
													methodCall = mi1.toString();
													temp.add(methodCall);
												}
											}
										}
									}
								}
							}
							methodCall = mi.toString();
							temp.add(methodCall);
						}
					}
					allExpressions.remove(pos);
				}
				for (int i = 0; i < allExpressions.size(); i++) {
					if (validAsUsage(allExpressions.get(i).getLeftOperand().toString())) {
						temp.add(allExpressions.get(i).getLeftOperand().toString());
					}
					if (validAsUsage(allExpressions.get(i).getRightOperand().toString())) {
						temp.add(allExpressions.get(i).getRightOperand().toString());

					}
				}
				return temp;
			}

			public ArrayList<String> getAllLeftOperandsInExpression(InfixExpression infx) {

				Expression e;
				MethodInvocation mi1;
				String methodCall;

				ArrayList<InfixExpression> allExpressions = new ArrayList<InfixExpression>();
				ArrayList<String> temp = new ArrayList<String>();

				allExpressions.add(infx);
				// loop that searches all infix expressions and splits them
				while (firstNotPrimitiveExpressions(allExpressions) != -1) {
					int pos = firstNotPrimitiveExpressions(allExpressions);
					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
						ConditionalExpression ie = (ConditionalExpression) allExpressions.get(pos).getLeftOperand();
						handleConditionalExpressions(ie.getElseExpression());
						handleConditionalExpressions(ie.getThenExpression());
						handleConditionalExpressions(ie.getExpression());
					}
					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
						ConditionalExpression ie = (ConditionalExpression) allExpressions.get(pos).getRightOperand();
						handleConditionalExpressions(ie.getElseExpression());
						handleConditionalExpressions(ie.getThenExpression());
						handleConditionalExpressions(ie.getExpression());
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						allExpressions.add((InfixExpression) allExpressions.get(pos).getLeftOperand());
					}
					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						allExpressions.add((InfixExpression) allExpressions.get(pos).getRightOperand());
					}
					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
						InstanceofExpression ioe = (InstanceofExpression) allExpressions.get(pos).getLeftOperand();
						temp.add(ioe.getLeftOperand().toString());
					}
					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
						InstanceofExpression ioe = (InstanceofExpression) allExpressions.get(pos).getRightOperand();
						temp.add(ioe.getLeftOperand().toString());
					}

					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
						CastExpression ce = (CastExpression) allExpressions.get(pos).getRightOperand();
						if (ce.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							printArguments((MethodInvocation) ce.getExpression());
							temp.add(ce.getExpression().toString());
						} else {
							temp.add(ce.getExpression().toString());
						}
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
						CastExpression ce = (CastExpression) allExpressions.get(pos).getLeftOperand();
						if (ce.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							printArguments((MethodInvocation) ce.getExpression());
							temp.add(ce.getExpression().toString());
						} else {
							temp.add(ce.getExpression().toString());
						}
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						MethodInvocation mi = (MethodInvocation) allExpressions.get(pos).getLeftOperand();
						printArguments(mi);
						methodCall = mi.toString();
						temp.add(methodCall);
						if (mi.getExpression() != null) {
							e = mi.getExpression();
							if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								mi1 = (MethodInvocation) e;
								printArguments(mi1);
								methodCall = mi1.toString();
								temp.add(methodCall);
								if (mi1.getExpression() != null) {
									e = mi1.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
											}
										}
									}
								}
							}
						}
						methodCall = mi.toString();
						temp.add(methodCall);
					}
					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						MethodInvocation mi = (MethodInvocation) allExpressions.get(pos).getRightOperand();
						methodCall = mi.toString();
						temp.add(methodCall);
						printArguments(mi);
						if (mi.getExpression() != null) {
							e = mi.getExpression();
							if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								mi1 = (MethodInvocation) e;
								printArguments(mi1);
								methodCall = mi1.toString();
								temp.add(methodCall);
								if (mi1.getExpression() != null) {
									e = mi1.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
											}
										}
									}
								}
							}
						}
						methodCall = mi.toString();
						temp.add(methodCall);
					}

					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
						temp.add(allExpressions.get(pos).getLeftOperand().toString());
					}

					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
						temp.add(allExpressions.get(pos).getRightOperand().toString());
					}

					// special case of having a parenthesis as a left/right operand
					if (allExpressions.get(pos).getLeftOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
						ParenthesizedExpression pe = (ParenthesizedExpression) allExpressions.get(pos).getLeftOperand();

						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
							ConditionalExpression ie = (ConditionalExpression) pe.getExpression();
							handleConditionalExpressions(ie.getElseExpression());
							handleConditionalExpressions(ie.getThenExpression());
							handleConditionalExpressions(ie.getExpression());
						}

						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
								// allExpressions.add((InfixExpression)
								// allExpressions.get(pos).getLeftOperand());
								allExpressions.add((InfixExpression) ie.getLeftOperand());

							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
								InstanceofExpression ioe = (InstanceofExpression) ie.getLeftOperand();
								temp.add(ioe.getLeftOperand().toString());
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								MethodInvocation mi = (MethodInvocation) ie.getLeftOperand();
								printArguments(mi);
								if (mi.getExpression() != null) {
									e = mi.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
												if (mi1.getExpression() != null) {
													e = mi1.getExpression();
													if (e.getClass().toString().equals(
															"class org.eclipse.jdt.core.dom.MethodInvocation")) {
														mi1 = (MethodInvocation) e;
														printArguments(mi1);
														methodCall = mi1.toString();
														temp.add(methodCall);
													}
												}
											}
										}
									}
								}
								methodCall = mi.toString();
								temp.add(methodCall);
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
								temp.add(ie.getLeftOperand().toString());
							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
								// allExpressions.add((InfixExpression)
								// allExpressions.get(pos).getRightOperand());
								allExpressions.add((InfixExpression) ie.getRightOperand());
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
								InstanceofExpression ioe = (InstanceofExpression) ie.getRightOperand();
								temp.add(ioe.getLeftOperand().toString());
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								MethodInvocation mi = (MethodInvocation) ie.getRightOperand();
								printArguments(mi);
								if (mi.getExpression() != null) {
									e = mi.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
												if (mi1.getExpression() != null) {
													e = mi1.getExpression();
													if (e.getClass().toString().equals(
															"class org.eclipse.jdt.core.dom.MethodInvocation")) {
														mi1 = (MethodInvocation) e;
														printArguments(mi1);
														methodCall = mi1.toString();
														temp.add(methodCall);
													}
												}
											}
										}
									}
								}
								methodCall = mi.toString();
								temp.add(methodCall);
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
								temp.add(ie.getRightOperand().toString());
							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
							InstanceofExpression ioe = (InstanceofExpression) pe.getExpression();
							temp.add(ioe.getLeftOperand().toString());
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
							InstanceofExpression ioe = (InstanceofExpression) pe.getExpression();
							temp.add(ioe.getRightOperand().toString());
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.Assignment")) {
							Assignment ass = (Assignment) pe.getExpression();
							temp.add(ass.getLeftHandSide().toString());
							if (validAsUsage(ass.getRightHandSide().toString())) {
								temp.add(ass.getRightHandSide().toString());

							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							MethodInvocation mi = (MethodInvocation) pe.getExpression();
							printArguments(mi);
							if (mi.getExpression() != null) {
								e = mi.getExpression();
								if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi1 = (MethodInvocation) e;
									printArguments(mi1);
									methodCall = mi1.toString();
									temp.add(methodCall);
									if (mi1.getExpression() != null) {
										e = mi1.getExpression();
										if (e.getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi1 = (MethodInvocation) e;
											printArguments(mi1);
											methodCall = mi1.toString();
											temp.add(methodCall);
											if (mi1.getExpression() != null) {
												e = mi1.getExpression();
												if (e.getClass().toString()
														.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
													mi1 = (MethodInvocation) e;
													printArguments(mi1);
													methodCall = mi1.toString();
													temp.add(methodCall);
												}
											}
										}
									}
								}
							}
							methodCall = mi.toString();
							temp.add(methodCall);
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							MethodInvocation mi = (MethodInvocation) pe.getExpression();
							printArguments(mi);
							if (mi.getExpression() != null) {
								e = mi.getExpression();
								if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi1 = (MethodInvocation) e;
									printArguments(mi1);
									methodCall = mi1.toString();
									temp.add(methodCall);
									if (mi1.getExpression() != null) {
										e = mi1.getExpression();
										if (e.getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi1 = (MethodInvocation) e;
											printArguments(mi1);
											methodCall = mi1.toString();
											temp.add(methodCall);
											if (mi1.getExpression() != null) {
												e = mi1.getExpression();
												if (e.getClass().toString()
														.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
													mi1 = (MethodInvocation) e;
													printArguments(mi1);
													methodCall = mi1.toString();
													temp.add(methodCall);
												}
											}
										}
									}
								}
							}
							methodCall = mi.toString();
							temp.add(methodCall);
						}
					}
					if (allExpressions.get(pos).getRightOperand().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
						ParenthesizedExpression pe = (ParenthesizedExpression) allExpressions.get(pos)
								.getRightOperand();

						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
							ConditionalExpression ie = (ConditionalExpression) pe.getExpression();
							handleConditionalExpressions(ie.getElseExpression());
							handleConditionalExpressions(ie.getThenExpression());
							handleConditionalExpressions(ie.getExpression());
						}

						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
								// allExpressions.add((InfixExpression)
								// allExpressions.get(pos).getLeftOperand());
								allExpressions.add((InfixExpression) ie.getLeftOperand());
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
								InstanceofExpression ioe = (InstanceofExpression) ie.getLeftOperand();
								temp.add(ioe.getLeftOperand().toString());
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								MethodInvocation mi = (MethodInvocation) ie.getLeftOperand();
								printArguments(mi);
								if (mi.getExpression() != null) {
									e = mi.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
												if (mi1.getExpression() != null) {
													e = mi1.getExpression();
													if (e.getClass().toString().equals(
															"class org.eclipse.jdt.core.dom.MethodInvocation")) {
														mi1 = (MethodInvocation) e;
														printArguments(mi1);
														methodCall = mi1.toString();
														temp.add(methodCall);
													}
												}
											}
										}
									}
								}
								methodCall = mi.toString();
								temp.add(methodCall);
							}
							if (ie.getLeftOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
								temp.add(ie.getLeftOperand().toString());
							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
								// allExpressions.add((InfixExpression)
								// allExpressions.get(pos).getRightOperand());
								allExpressions.add((InfixExpression) ie.getRightOperand());
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
								InstanceofExpression ioe = (InstanceofExpression) ie.getRightOperand();
								temp.add(ioe.getLeftOperand().toString());
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								MethodInvocation mi = (MethodInvocation) ie.getRightOperand();
								printArguments(mi);
								if (mi.getExpression() != null) {
									e = mi.getExpression();
									if (e.getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi1 = (MethodInvocation) e;
										printArguments(mi1);
										methodCall = mi1.toString();
										temp.add(methodCall);
										if (mi1.getExpression() != null) {
											e = mi1.getExpression();
											if (e.getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi1 = (MethodInvocation) e;
												printArguments(mi1);
												methodCall = mi1.toString();
												temp.add(methodCall);
												if (mi1.getExpression() != null) {
													e = mi1.getExpression();
													if (e.getClass().toString().equals(
															"class org.eclipse.jdt.core.dom.MethodInvocation")) {
														mi1 = (MethodInvocation) e;
														printArguments(mi1);
														methodCall = mi1.toString();
														temp.add(methodCall);
													}
												}
											}
										}
									}
								}
								methodCall = mi.toString();
								temp.add(methodCall);
							}
							if (ie.getRightOperand().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
								temp.add(ie.getRightOperand().toString());
							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
							InstanceofExpression ioe = (InstanceofExpression) pe.getExpression();
							temp.add(ioe.getLeftOperand().toString());
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
							InstanceofExpression ioe = (InstanceofExpression) pe.getExpression();
							temp.add(ioe.getRightOperand().toString());
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.Assignment")) {
							Assignment ass = (Assignment) pe.getExpression();
							temp.add(ass.getLeftHandSide().toString());
							if (validAsUsage(ass.getRightHandSide().toString())) {
								temp.add(ass.getRightHandSide().toString());

							}
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							MethodInvocation mi = (MethodInvocation) pe.getExpression();
							printArguments(mi);
							if (mi.getExpression() != null) {
								e = mi.getExpression();
								if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi1 = (MethodInvocation) e;
									printArguments(mi1);
									methodCall = mi1.toString();
									temp.add(methodCall);
									if (mi1.getExpression() != null) {
										e = mi1.getExpression();
										if (e.getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi1 = (MethodInvocation) e;
											printArguments(mi1);
											methodCall = mi1.toString();
											temp.add(methodCall);
											if (mi1.getExpression() != null) {
												e = mi1.getExpression();
												if (e.getClass().toString()
														.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
													mi1 = (MethodInvocation) e;
													printArguments(mi1);
													methodCall = mi1.toString();
													temp.add(methodCall);
												}
											}
										}
									}
								}
							}
							methodCall = mi.toString();
							temp.add(methodCall);
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							MethodInvocation mi = (MethodInvocation) pe.getExpression();
							printArguments(mi);
							if (mi.getExpression() != null) {
								e = mi.getExpression();
								if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi1 = (MethodInvocation) e;
									printArguments(mi1);
									methodCall = mi1.toString();
									temp.add(methodCall);
									if (mi1.getExpression() != null) {
										e = mi1.getExpression();
										if (e.getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi1 = (MethodInvocation) e;
											printArguments(mi1);
											methodCall = mi1.toString();
											temp.add(methodCall);
											if (mi1.getExpression() != null) {
												e = mi1.getExpression();
												if (e.getClass().toString()
														.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
													mi1 = (MethodInvocation) e;
													printArguments(mi1);
													methodCall = mi1.toString();
													temp.add(methodCall);
												}
											}
										}
									}
								}
							}
							methodCall = mi.toString();
							temp.add(methodCall);
						}
					}
					allExpressions.remove(pos);
				}
				for (int i = 0; i < allExpressions.size(); i++) {
					if (validAsUsage(allExpressions.get(i).getLeftOperand().toString())) {
						temp.add(allExpressions.get(i).getLeftOperand().toString());
					}
					if (validAsUsage(allExpressions.get(i).getRightOperand().toString())) {
						temp.add(allExpressions.get(i).getRightOperand().toString());
					}

				}
				return temp;
			}

			public int countOccurencesOf(String s, char c) {
				int temp = 0;
				int pos = 0;
				pos = s.indexOf(c);
				if (pos != -1) {
					temp++;
				}

				while (pos != -1) {
					pos = s.indexOf(c, pos + 1);
					if (pos != -1) {
						temp++;
					}
				}
				return temp;
			}

			public String checkObject(MethodInvocation invoc) {
				String first_part = "-1";

				Expression e = invoc.getExpression();
				if (e != null) {
					if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.CastExpression")) {
						CastExpression e1 = (CastExpression) e;
						first_part = e1.getExpression().toString();
					} else if (e.getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
						ParenthesizedExpression pe = (ParenthesizedExpression) e;
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
							pe = (ParenthesizedExpression) pe.getExpression();
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
							pe = (ParenthesizedExpression) pe.getExpression();
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
							pe = (ParenthesizedExpression) pe.getExpression();
						}
						if (pe.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
							CastExpression e1 = (CastExpression) pe.getExpression();
							first_part = e1.getExpression().toString();
						} else {
							first_part = pe.getExpression().toString();
						}
					} else if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
						ConditionalExpression co = (ConditionalExpression) e;
						handleConditionalExpressions(co.getElseExpression());
						handleConditionalExpressions(co.getThenExpression());
						handleConditionalExpressions(co.getExpression());
						first_part = "-1";
					}
				}
				return first_part;
			}

			public String cleanUpMethodName(String methodCall, MethodInvocation invoc) {

				String temp_str = "-1";

				if (invoc != null) {
					temp_str = checkObject(invoc);
				}

				if (temp_str.equals("-1")) {
					String firstPart = "";
					int openedParenthesis = 0, closedParenthesis = 0;

					if (methodCall.indexOf('(') == 0) {
						String[] temp = methodCall.split("\\.");
						firstPart = temp[0];
						temp = temp[0].split("\\)");
						if (temp.length > 1) {
							firstPart = temp[1].trim();
						}
						firstPart = firstPart.replaceAll("\\(", "");
						firstPart = firstPart.replaceAll("\\)", "");
					} else {
						firstPart = methodCall.replaceAll("\\(.*", "");
					}
					if (firstPart.startsWith("\"")) {
						firstPart = "-1";
					} else {
						String[] temp = methodCall.split("\\.");
						for (int j = 1; j < temp.length; j++) {
							if(temp[j - 1].length()==0) {	//***TEMP_FIX
								continue;
							}
							if (temp[j - 1].charAt(temp[j - 1].length() - 1) == ')') {
								for (int i = 0; i < j; i++) {
									openedParenthesis = openedParenthesis + countOccurencesOf(temp[i], '(');
									closedParenthesis = closedParenthesis + countOccurencesOf(temp[i], ')');
								}
								if (openedParenthesis == closedParenthesis) {
									firstPart = firstPart + "." + temp[j].replaceAll("\\(.*", "");
								}
							}
						}

						if (firstPart.contains("new ")) {
							firstPart = firstPart.replace("new ", "");
							if (firstPart.contains(".")) {
								String temp1[] = firstPart.split("\\.");
								firstPart = temp1[0] + ".constructor";
								for (int j = 1; j < temp1.length; j++) {
									firstPart = firstPart + "." + temp1[j];
								}
							} else {
								firstPart = firstPart + ".constructor";
							}
						}
						if (firstPart.startsWith("!")) {
							firstPart = firstPart.replaceFirst("!", "");
						}

						if (firstPart.contains(" ")) {
							String innerMethodAlert[] = firstPart.split(" ");
							firstPart = innerMethodAlert[0];
							if (firstPart.contains("\n")) {
								firstPart = firstPart.replace("\n", "");
							}
						}
					}

					return firstPart;
				} else {
					temp_str = temp_str + "." + invoc.getName();
					return temp_str;
				}
			}

			public boolean visit(VariableDeclarationFragment node) {

				SimpleName name = node.getName();
				defined_variables.add(name.getIdentifier());
				if (node.getInitializer() != null) {

					if (node.getInitializer().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
						CastExpression ce = (CastExpression) node.getInitializer();
						if (ce.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							printArguments((MethodInvocation) ce.getExpression());
							printToFile("Invocation",
									cleanUpMethodName(ce.getExpression().toString(),
											(MethodInvocation) ce.getExpression()),
									cu.getLineNumber(node.getStartPosition()));
						} else {
							printToFile("Usage", ce.getExpression().toString(),
									cu.getLineNumber(node.getStartPosition()));
						}
					}

					if (node.getInitializer().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.SimpleName")) {
						if (validAsUsage(node.getInitializer().toString())) {
							printToFile("Usage", node.getInitializer().toString(),
									cu.getLineNumber(node.getStartPosition()));
						}
					}

					if (node.getInitializer().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {

						MethodInvocation mi = (MethodInvocation) node.getInitializer();
						String methodCall = mi.toString();
						addParametersAsUsage(methodCall, node);
						methodCall = cleanUpMethodName(methodCall, mi);
						if (!methodCall.contains("new ")) {
							printArguments(mi);
							printToFile("Invocation", methodCall, cu.getLineNumber(node.getStartPosition()));
						}
						if (mi.getExpression() != null) {
							if (mi.getExpression().getClass().toString()
									.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
								mi = (MethodInvocation) mi.getExpression();
								printArguments(mi);
								methodCall = mi.toString();
								addParametersAsUsage(methodCall, node);
								methodCall = cleanUpMethodName(methodCall, mi);
								if (!methodCall.contains("new ")) {
									printToFile("Invocation", methodCall, cu.getLineNumber(node.getStartPosition()));
								}
								if (mi.getExpression() != null) {
									if (mi.getExpression().getClass().toString()
											.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
										mi = (MethodInvocation) mi.getExpression();
										printArguments(mi);
										methodCall = mi.toString();
										addParametersAsUsage(methodCall, node);
										methodCall = cleanUpMethodName(methodCall, mi);
										if (!methodCall.contains("new ")) {
											printToFile("Invocation", methodCall,
													cu.getLineNumber(node.getStartPosition()));
										}
										if (mi.getExpression() != null) {
											if (mi.getExpression().getClass().toString()
													.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
												mi = (MethodInvocation) mi.getExpression();
												printArguments(mi);
												methodCall = mi.toString();
												addParametersAsUsage(methodCall, node);
												methodCall = cleanUpMethodName(methodCall, mi);
												if (!methodCall.contains("new ")) {
													printToFile("Invocation", methodCall,
															cu.getLineNumber(node.getStartPosition()));
												}
											}
										}
									}
								}
							}
						}
					}

					if (node.getInitializer().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ClassInstanceCreation")) {
						String methodCall = node.getInitializer().toString();
						addParametersAsUsage(methodCall, node);
						methodCall = cleanUpMethodName(methodCall, null);
						printToFile("Invocation", methodCall, cu.getLineNumber(node.getStartPosition()));
					}

					if (node.getInitializer().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						InfixExpression ie = (InfixExpression) node.getInitializer();
						ArrayList<String> temp = getAllLeftOperandsInAssignmentExpression(ie);
						for (int i = 0; i < temp.size(); i++) {
							if (temp.get(i).toString().contains("(")) {
								printToFile("Invocation", cleanUpMethodName(temp.get(i).toString(), null),
										cu.getLineNumber(node.getStartPosition()));
							} else {
								printToFile("Usage", temp.get(i).toString(), cu.getLineNumber(node.getStartPosition()));
							}
						}
					}

					if (node.getInitializer().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
						ConditionalExpression ie = (ConditionalExpression) node.getInitializer();
						handleConditionalExpressions(ie.getElseExpression());
						handleConditionalExpressions(ie.getThenExpression());
						handleConditionalExpressions(ie.getExpression());
					}

					if (node.getInitializer().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.FieldAccess")) {
						FieldAccess fa = (FieldAccess) node.getInitializer();
						if (fa.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							printToFile("Invocation",
									cleanUpMethodName(fa.getExpression().toString(),
											(MethodInvocation) fa.getExpression()),
									cu.getLineNumber(node.getStartPosition()));
						}
						printToFile("Usage", cleanUpMethodName(fa.toString(), null),
								cu.getLineNumber(node.getStartPosition()));
					}

				}
				printToFile("Declaration", name.toString(), cu.getLineNumber(name.getStartPosition()));
				return false;
			}

			public boolean visit(ClassInstanceCreation node) {
				String methodCall = node.toString();

				addParametersAsUsage(methodCall, node);
				methodCall = cleanUpMethodName(methodCall, null);
				printToFile("Invocation", methodCall, cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ReturnStatement node) {
				String out = "Return#null#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(BreakStatement node) {
				String out = "Break#null#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(ContinueStatement node) {
				String out = "Continue#null#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(FieldAccess node) {
				printToFile("Usage", node.toString().replace("this.", ""), cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(SuperFieldAccess node) {
				printToFile("Usage", node.toString().replace("this.", ""), cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(QualifiedName node) {
				printToFile("Usage", node.toString(), cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(CastExpression node) {
				if (node.getExpression().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
					printToFile("Invocation",
							cleanUpMethodName(node.getExpression().toString(), (MethodInvocation) node.getExpression()),
							cu.getLineNumber(node.getStartPosition()));
				} else if (node.getExpression().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
					InfixExpression ie = (InfixExpression) node.getExpression();
					ArrayList<String> temp = getAllLeftOperandsInExpression(ie);
					for (int i = 0; i < temp.size(); i++) {
						if (temp.get(i).toString().contains("(")) {
							printToFile("Invocation", cleanUpMethodName(temp.get(i).toString(), null),
									cu.getLineNumber(node.getStartPosition()));
						} else {
							printToFile("Usage", temp.get(i).toString(), cu.getLineNumber(node.getStartPosition()));
						}
					}
				} else if (node.getExpression().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
					ParenthesizedExpression pe = (ParenthesizedExpression) node.getExpression();
					if (pe.getExpression().getClass().equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						InfixExpression ie = (InfixExpression) pe.getExpression();
						ArrayList<String> temp = getAllLeftOperandsInExpression(ie);
						for (int i = 0; i < temp.size(); i++) {
							if (temp.get(i).toString().contains("(")) {
								printToFile("Invocation", cleanUpMethodName(temp.get(i).toString(), null),
										cu.getLineNumber(node.getStartPosition()));
							} else {
								printToFile("Usage", temp.get(i).toString(), cu.getLineNumber(node.getStartPosition()));
							}
						}
					}
				} else {
					printToFile("Usage", node.getExpression().toString(), cu.getLineNumber(node.getStartPosition()));
				}
				return true;
			}

			public boolean visit(Assignment node) {

				if (!defined_variables.contains(node.getLeftHandSide())
						&& not_counted_lines.contains(cu.getLineNumber(node.getStartPosition())) == false) {
					printToFile("Usage-ASSIGN", node.getLeftHandSide().toString(),
							cu.getLineNumber(node.getStartPosition()));
				}

				if (!defined_variables.contains(node.getRightHandSide())
						&& not_counted_lines.contains(cu.getLineNumber(node.getStartPosition())) == false) {
					// these if statements filter out any possible left-hand-side operands that are
					// not variables
					if (validAsUsage(node.getRightHandSide().toString())) {
						printToFile("Usage", node.getRightHandSide().toString(),
								cu.getLineNumber(node.getStartPosition()));
					}
				}

				if (node.getRightHandSide().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.CastExpression")) {
					CastExpression ce = (CastExpression) node.getRightHandSide();
					if (ce.getExpression().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						printArguments((MethodInvocation) ce.getExpression());
						printToFile("Invocation",
								cleanUpMethodName(ce.getExpression().toString(), (MethodInvocation) ce.getExpression()),
								cu.getLineNumber(node.getStartPosition()));
					} else if (ce.getExpression().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
						InfixExpression ie = (InfixExpression) ce.getExpression();
						ArrayList<String> temp = getAllLeftOperandsInExpression(ie);
						for (int i = 0; i < temp.size(); i++) {
							if (temp.get(i).toString().contains("(")) {
								printToFile("Invocation", cleanUpMethodName(temp.get(i).toString(), null),
										cu.getLineNumber(node.getStartPosition()));
							} else {
								printToFile("Usage", temp.get(i).toString(), cu.getLineNumber(node.getStartPosition()));
							}
						}
					} else if (ce.getExpression().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.ParenthesizedExpression")) {
						ParenthesizedExpression pe = (ParenthesizedExpression) ce.getExpression();
						if (pe.getExpression().getClass().equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
							InfixExpression ie = (InfixExpression) pe.getExpression();
							ArrayList<String> temp = getAllLeftOperandsInExpression(ie);
							for (int i = 0; i < temp.size(); i++) {
								if (temp.get(i).toString().contains("(")) {
									printToFile("Invocation", cleanUpMethodName(temp.get(i).toString(), null),
											cu.getLineNumber(node.getStartPosition()));
								} else {
									printToFile("Usage", temp.get(i).toString(),
											cu.getLineNumber(node.getStartPosition()));
								}
							}
						}
					} else {
						printToFile("Usage", ce.getExpression().toString(), cu.getLineNumber(node.getStartPosition()));
					}
				}

				if (node.getRightHandSide().getClass().toString().equals("class org.eclipse.jdt.core.dom.SimpleName")) {
					if (validAsUsage(node.getRightHandSide().toString())) {
						printToFile("Usage", node.getRightHandSide().toString(),
								cu.getLineNumber(node.getStartPosition()));
					}
				}

				if (node.getRightHandSide().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
					InfixExpression ie = (InfixExpression) node.getRightHandSide();
					ArrayList<String> temp = getAllLeftOperandsInAssignmentExpression(ie);
					for (int i = 0; i < temp.size(); i++) {
						if (temp.get(i).toString().contains("(")) {
							printToFile("Invocation", cleanUpMethodName(temp.get(i).toString(), null),
									cu.getLineNumber(node.getStartPosition()));
						} else {
							printToFile("Usage", temp.get(i).toString(), cu.getLineNumber(node.getStartPosition()));
						}
					}
				}

				if (node.getRightHandSide().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.ConditionalExpression")) {
					ConditionalExpression ie = (ConditionalExpression) node.getRightHandSide();
					handleConditionalExpressions(ie.getElseExpression());
					handleConditionalExpressions(ie.getThenExpression());
					handleConditionalExpressions(ie.getExpression());
				}

				if (node.getRightHandSide().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.FieldAccess")) {
					FieldAccess fa = (FieldAccess) node.getRightHandSide();
					if (fa.getExpression().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
						printArguments((MethodInvocation) fa.getExpression());
						printToFile("Invocation",
								cleanUpMethodName(fa.getExpression().toString(), (MethodInvocation) fa.getExpression()),
								cu.getLineNumber(node.getStartPosition()));
					}
					printToFile("Usage", cleanUpMethodName(fa.toString(), null),
							cu.getLineNumber(node.getStartPosition()));
				}

				if (node.getRightHandSide().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {

					MethodInvocation mi = (MethodInvocation) node.getRightHandSide();
					String methodCall = mi.toString();
					addParametersAsUsage(methodCall, node);
					printArguments(mi);
					methodCall = cleanUpMethodName(methodCall, mi);
					if (!methodCall.contains("new ")) {
						printToFile("Invocation", methodCall, cu.getLineNumber(node.getStartPosition()));
					}
					if (mi.getExpression() != null) {
						if (mi.getExpression().getClass().toString()
								.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
							mi = (MethodInvocation) mi.getExpression();
							printArguments(mi);
							methodCall = mi.toString();
							addParametersAsUsage(methodCall, node);
							methodCall = cleanUpMethodName(methodCall, mi);
							if (!methodCall.contains("new ")) {
								printToFile("Invocation", methodCall, cu.getLineNumber(node.getStartPosition()));
							}
							if (mi.getExpression() != null) {
								if (mi.getExpression().getClass().toString()
										.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
									mi = (MethodInvocation) mi.getExpression();
									printArguments(mi);
									methodCall = mi.toString();
									addParametersAsUsage(methodCall, node);
									methodCall = cleanUpMethodName(methodCall, mi);
									if (!methodCall.contains("new ")) {
										printToFile("Invocation", methodCall,
												cu.getLineNumber(node.getStartPosition()));
									}
									if (mi.getExpression() != null) {
										if (mi.getExpression().getClass().toString()
												.equals("class org.eclipse.jdt.core.dom.MethodInvocation")) {
											mi = (MethodInvocation) mi.getExpression();
											printArguments(mi);
											methodCall = mi.toString();
											addParametersAsUsage(methodCall, node);
											methodCall = cleanUpMethodName(methodCall, mi);
											if (!methodCall.contains("new ")) {
												printToFile("Invocation", methodCall,
														cu.getLineNumber(node.getStartPosition()));
											}
										}
									}
								}
							}
						}
					}
				}

				return true;
			}

			public boolean visit(SimpleName node) {

				if (defined_variables.contains(node.getIdentifier())
						&& not_counted_lines.contains(cu.getLineNumber(node.getStartPosition())) == false) {
					boolean found = false;
					if (not_used_due_to_invocation.containsKey(cu.getLineNumber(node.getStartPosition()))) {
						ArrayList<String> alreadyDeclearedMethods = not_used_due_to_invocation
								.get(cu.getLineNumber(node.getStartPosition()));
						for (int i = 0; i < alreadyDeclearedMethods.size(); i++) {
							if (alreadyDeclearedMethods.get(i).contains(node.getIdentifier().replace("'", ""))) {
								found = true;
								break;
							}
						}
					}
					if (found == false) {
						printToFile("Usage", node.getIdentifier().replace("'", ""),
								cu.getLineNumber(node.getStartPosition()));
					}
				} else {
					// FIXME
				}
				return true;
			}

			public boolean visit(MethodInvocation node) {

				String methodCall = node.toString();
				methodCall = cleanUpMethodName(methodCall, node);
				for (Integer[] key : ifStatements.keySet()) {
					if (key[0] == cu.getLineNumber(node.getStartPosition())) {
						ArrayList<String> temp_if = ifStatements.get(key);
						temp_if.add(methodCall);
						ifStatements.put(key, temp_if);
					}
				}

				if (not_used_due_to_invocation.containsKey(cu.getLineNumber(node.getStartPosition()))) {
					ArrayList<String> temp = not_used_due_to_invocation.get(cu.getLineNumber(node.getStartPosition()));
					temp.add(methodCall);
					Integer line_num = cu.getLineNumber(node.getStartPosition());
					not_used_due_to_invocation.put(line_num, temp);
				} else {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(methodCall);
					Integer line_num = cu.getLineNumber(node.getStartPosition());
					not_used_due_to_invocation.put(line_num, temp);
				}

				if (!methodCall.contains("new ")) {
					printToFile("Invocation", methodCall, cu.getLineNumber(node.getStartPosition()));
				}
				printArguments(node);
				return true;
			}

			public boolean visit(DoStatement node) {
				String out = "BEGIN_DO#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				out = "END_DO#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(ConditionalExpression node) {
				String out = "BEGIN_CONDITIONAL#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				out = "END_CONDITIONAL#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(EnhancedForStatement node) {
				String[] temp = node.getParameter().toString().split(" ");
				String out = "BEGIN_FOR#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				defined_variables.add(temp[1]);
				printToFile("Declaration", temp[1], cu.getLineNumber(node.getStartPosition()));
				out = "END_FOR#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(ForStatement node) {
				String out = "BEGIN_FOR#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				out = "END_FOR#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(SwitchStatement node) {
				String out = "BEGIN_SWITCH#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				out = "END_SWITCH#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);
				Integer[] switchstartend = new Integer[2];
				switchstartend[0] = cu.getLineNumber(node.getStartPosition());
				switchstartend[1] = cu.getLineNumber(node.getStartPosition() + node.getLength());
				switchStatements.put(switchstartend, node.getExpression().toString());
				return true;
			}

			public boolean visit(SwitchCase node) {
				String out = "BEGIN_CASE#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);

				Integer[] casestartend = new Integer[2];
				casestartend[0] = cu.getLineNumber(node.getStartPosition());
				casestartend[1] = cu.getLineNumber(node.getStartPosition() + node.getLength());
				for (Integer[] key : switchStatements.keySet()) {
					if (key[0] < casestartend[0] && key[1] > casestartend[1]) {
						if (switchStatements.get(key).contains(".")) {
							printToFile("Usage-IF", switchStatements.get(key),
									cu.getLineNumber(node.getStartPosition()));
						} else {
							printToFile("Invocation-IF", cleanUpMethodName(switchStatements.get(key), null),
									cu.getLineNumber(node.getStartPosition()));
						}
					}
				}

				return true;
			}

			public boolean visit(WhileStatement node) {
				String out = "BEGIN_WHILE#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				out = "END_WHILE#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(TryStatement node) {
				String out = "BEGIN_TRY#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				out = "END_TRY#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(CatchClause node) {
				String[] temp = node.getException().toString().split(" ");
				defined_variables.add(temp[1]);
				String out = "BEGIN_CATCH#" + cu.getLineNumber(node.getStartPosition()) + ";";
				output.add(out);
				printToFile("Usage", temp[1], cu.getLineNumber(node.getStartPosition()));
				out = "END_CATCH#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);
				return true;
			}

			public boolean visit(IfStatement node) {

				Integer[] ifstartend = new Integer[2];
				ifstartend[0] = cu.getLineNumber(node.getStartPosition());
				ifstartend[1] = cu.getLineNumber(node.getStartPosition() + node.getLength());

				if (node.getExpression().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.InfixExpression")) {
					InfixExpression infx = (InfixExpression) node.getExpression();
					ifStatements.put(ifstartend, getAllLeftOperandsInExpression(infx));
				} else if (node.getExpression().getClass().toString()
						.equals("class org.eclipse.jdt.core.dom.InstanceofExpression")) {
					InstanceofExpression ioe = (InstanceofExpression) node.getExpression();
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(ioe.getLeftOperand().toString());
					ifStatements.put(ifstartend, temp);
				} else {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(node.getExpression().toString());
					ifStatements.put(ifstartend, temp);
				}

				if (!elseIfLines.contains(cu.getLineNumber(node.getStartPosition()))) {
					String out = "BEGIN_IF#" + cu.getLineNumber(node.getStartPosition()) + ";";
					output.add(out);
				}
				if (node.getElseStatement() != null) {
					String out = "BEGIN_ELSE#" + cu.getLineNumber(node.getElseStatement().getStartPosition()) + ";";
					output.add(out);
					if (node.getElseStatement().getClass().toString()
							.equals("class org.eclipse.jdt.core.dom.IfStatement")) {
						elseIfLines.add(cu.getLineNumber(node.getElseStatement().getStartPosition()));
					}
					for (Integer[] key : ifStatements.keySet()) {
						if (key[0] < cu.getLineNumber(node.getElseStatement().getStartPosition())
								&& key[1] >= cu.getLineNumber(node.getElseStatement().getStartPosition())) {
							for (int i = 0; i < ifStatements.get(key).size(); i++) {
								if (ifStatements.get(key).get(i).contains("(")) {
									printToFile("Invocation-IF",
											cleanUpMethodName(ifStatements.get(key).get(i).trim(), null),
											cu.getLineNumber(node.getElseStatement().getStartPosition()));
								} else {
									printToFile("Usage-IF", ifStatements.get(key).get(i).trim(),
											cu.getLineNumber(node.getElseStatement().getStartPosition()));
								}
							}
						}
					}
				}
				String out = "END_IF#" + cu.getLineNumber(node.getStartPosition() + node.getLength()) + ";";
				output.add(out);

				for (Integer[] key : ifStatements.keySet()) {
					if (key[0] < ifstartend[0] && key[1] >= ifstartend[1]) {
						for (int i = 0; i < ifStatements.get(key).size(); i++) {
							if (ifStatements.get(key).get(i).contains("(")) {
								printToFile("Invocation", cleanUpMethodName(ifStatements.get(key).get(i).trim(), null),
										ifstartend[0]);
							} else {
								printToFile("Usage", ifStatements.get(key).get(i).trim(), ifstartend[0]);
							}
						}
					}
				}

				return true;
			}

			public boolean visit(MethodDeclaration node) {
				SimpleName name = node.getName();

				String out = "Method:" + name;
				List<SingleVariableDeclaration> list = node.parameters();
				String params = "(";

				for (SingleVariableDeclaration par : list) {
					params += par.getType() + ",";
				}
				if (list != null && list.size() > 0) {
					params = params.substring(0, params.length() - 1);
				}

				params += ");";
				output.add(out + params);
				for (int i = 0; i < node.parameters().size(); i++) {
					String full_parameter = node.parameters().get(i).toString();
					String[] parameter = full_parameter.split(" ");
					defined_variables.add(parameter[1]);
					out = "parameter#" + parameter[1] + ";";
					output.add(out);
					not_counted_lines.add(cu.getLineNumber(name.getStartPosition()));
				}
				return true;
			}
		});
	}
}
