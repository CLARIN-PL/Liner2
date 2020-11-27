package g419.serel.parseRule;// Generated from /home/user57/NLPWR/projects/Liner2/g419-serel-core/src/main/antlr/ParseRule.g4 by ANTLR 4.8

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ParseRuleParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface ParseRuleVisitor<T> extends ParseTreeVisitor<T> {
  /**
   * Visit a parse tree produced by {@link ParseRuleParser#start}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitStart(ParseRuleParser.StartContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#semRel}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitSemRel(ParseRuleParser.SemRelContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#semRelName}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitSemRelName(ParseRuleParser.SemRelNameContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#expression}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitExpression(ParseRuleParser.ExpressionContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#rootNode}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitRootNode(ParseRuleParser.RootNodeContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#rootLeftExpression}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitRootLeftExpression(ParseRuleParser.RootLeftExpressionContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#rootRightExpression}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitRootRightExpression(ParseRuleParser.RootRightExpressionContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#leftExpression}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitLeftExpression(ParseRuleParser.LeftExpressionContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#rightExpression}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitRightExpression(ParseRuleParser.RightExpressionContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#leftEdge}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitLeftEdge(ParseRuleParser.LeftEdgeContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#rightEdge}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitRightEdge(ParseRuleParser.RightEdgeContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#depRel}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitDepRel(ParseRuleParser.DepRelContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#depRelValue}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitDepRelValue(ParseRuleParser.DepRelValueContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#node}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitNode(ParseRuleParser.NodeContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#xPos}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitXPos(ParseRuleParser.XPosContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#xPosValue}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitXPosValue(ParseRuleParser.XPosValueContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#element}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitElement(ParseRuleParser.ElementContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#namedEntityToRole}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitNamedEntityToRole(ParseRuleParser.NamedEntityToRoleContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#namedEntity}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitNamedEntity(ParseRuleParser.NamedEntityContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#role}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitRole(ParseRuleParser.RoleContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#text}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitText(ParseRuleParser.TextContext ctx);

  /**
   * Visit a parse tree produced by {@link ParseRuleParser#id}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  T visitId(ParseRuleParser.IdContext ctx);
}