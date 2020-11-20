// Generated from /home/user57/NLPWR/projects/Liner2/g419-serel-core/src/main/antlr/SerelRuleMatcher.g4 by ANTLR 4.8
package g419.serel.ruleMatcher;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SerelRuleMatcherParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SerelRuleMatcherVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(SerelRuleMatcherParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SerelRuleMatcherParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#semRelName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSemRelName(SerelRuleMatcherParser.SemRelNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#depRelValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDepRelValue(SerelRuleMatcherParser.DepRelValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#depRel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDepRel(SerelRuleMatcherParser.DepRelContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#leftEdge}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftEdge(SerelRuleMatcherParser.LeftEdgeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#rightEdge}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRightEdge(SerelRuleMatcherParser.RightEdgeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#namedEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedEntity(SerelRuleMatcherParser.NamedEntityContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#role}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole(SerelRuleMatcherParser.RoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitText(SerelRuleMatcherParser.TextContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#namedEntityToRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedEntityToRole(SerelRuleMatcherParser.NamedEntityToRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(SerelRuleMatcherParser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#xPosValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXPosValue(SerelRuleMatcherParser.XPosValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#xPos}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXPos(SerelRuleMatcherParser.XPosContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#token}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToken(SerelRuleMatcherParser.TokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#leftExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeftExpression(SerelRuleMatcherParser.LeftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#rightExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRightExpression(SerelRuleMatcherParser.RightExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SerelRuleMatcherParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(SerelRuleMatcherParser.IdContext ctx);
}