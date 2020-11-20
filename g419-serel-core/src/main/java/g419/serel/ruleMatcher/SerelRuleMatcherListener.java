// Generated from /home/user57/NLPWR/projects/Liner2/g419-serel-core/src/main/antlr/SerelRuleMatcher.g4 by ANTLR 4.8
package g419.serel.ruleMatcher;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SerelRuleMatcherParser}.
 */
public interface SerelRuleMatcherListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(SerelRuleMatcherParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(SerelRuleMatcherParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SerelRuleMatcherParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SerelRuleMatcherParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#semRelName}.
	 * @param ctx the parse tree
	 */
	void enterSemRelName(SerelRuleMatcherParser.SemRelNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#semRelName}.
	 * @param ctx the parse tree
	 */
	void exitSemRelName(SerelRuleMatcherParser.SemRelNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#depRelValue}.
	 * @param ctx the parse tree
	 */
	void enterDepRelValue(SerelRuleMatcherParser.DepRelValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#depRelValue}.
	 * @param ctx the parse tree
	 */
	void exitDepRelValue(SerelRuleMatcherParser.DepRelValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#depRel}.
	 * @param ctx the parse tree
	 */
	void enterDepRel(SerelRuleMatcherParser.DepRelContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#depRel}.
	 * @param ctx the parse tree
	 */
	void exitDepRel(SerelRuleMatcherParser.DepRelContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#leftEdge}.
	 * @param ctx the parse tree
	 */
	void enterLeftEdge(SerelRuleMatcherParser.LeftEdgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#leftEdge}.
	 * @param ctx the parse tree
	 */
	void exitLeftEdge(SerelRuleMatcherParser.LeftEdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#rightEdge}.
	 * @param ctx the parse tree
	 */
	void enterRightEdge(SerelRuleMatcherParser.RightEdgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#rightEdge}.
	 * @param ctx the parse tree
	 */
	void exitRightEdge(SerelRuleMatcherParser.RightEdgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#namedEntity}.
	 * @param ctx the parse tree
	 */
	void enterNamedEntity(SerelRuleMatcherParser.NamedEntityContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#namedEntity}.
	 * @param ctx the parse tree
	 */
	void exitNamedEntity(SerelRuleMatcherParser.NamedEntityContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#role}.
	 * @param ctx the parse tree
	 */
	void enterRole(SerelRuleMatcherParser.RoleContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#role}.
	 * @param ctx the parse tree
	 */
	void exitRole(SerelRuleMatcherParser.RoleContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#text}.
	 * @param ctx the parse tree
	 */
	void enterText(SerelRuleMatcherParser.TextContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#text}.
	 * @param ctx the parse tree
	 */
	void exitText(SerelRuleMatcherParser.TextContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#namedEntityToRole}.
	 * @param ctx the parse tree
	 */
	void enterNamedEntityToRole(SerelRuleMatcherParser.NamedEntityToRoleContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#namedEntityToRole}.
	 * @param ctx the parse tree
	 */
	void exitNamedEntityToRole(SerelRuleMatcherParser.NamedEntityToRoleContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(SerelRuleMatcherParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(SerelRuleMatcherParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#xPosValue}.
	 * @param ctx the parse tree
	 */
	void enterXPosValue(SerelRuleMatcherParser.XPosValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#xPosValue}.
	 * @param ctx the parse tree
	 */
	void exitXPosValue(SerelRuleMatcherParser.XPosValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#xPos}.
	 * @param ctx the parse tree
	 */
	void enterXPos(SerelRuleMatcherParser.XPosContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#xPos}.
	 * @param ctx the parse tree
	 */
	void exitXPos(SerelRuleMatcherParser.XPosContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#token}.
	 * @param ctx the parse tree
	 */
	void enterToken(SerelRuleMatcherParser.TokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#token}.
	 * @param ctx the parse tree
	 */
	void exitToken(SerelRuleMatcherParser.TokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#leftExpression}.
	 * @param ctx the parse tree
	 */
	void enterLeftExpression(SerelRuleMatcherParser.LeftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#leftExpression}.
	 * @param ctx the parse tree
	 */
	void exitLeftExpression(SerelRuleMatcherParser.LeftExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#rightExpression}.
	 * @param ctx the parse tree
	 */
	void enterRightExpression(SerelRuleMatcherParser.RightExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#rightExpression}.
	 * @param ctx the parse tree
	 */
	void exitRightExpression(SerelRuleMatcherParser.RightExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SerelRuleMatcherParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(SerelRuleMatcherParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerelRuleMatcherParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(SerelRuleMatcherParser.IdContext ctx);
}