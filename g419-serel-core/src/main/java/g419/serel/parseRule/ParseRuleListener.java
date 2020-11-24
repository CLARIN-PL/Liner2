// Generated from /home/user57/NLPWR/projects/CmdANTLR/src/ParseRule.g4 by ANTLR 4.8
package g419.serel.parseRule;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ParseRuleParser}.
 */
public interface ParseRuleListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link ParseRuleParser#start}.
     *
     * @param ctx the parse tree
     */
    void enterStart(ParseRuleParser.StartContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#start}.
     *
     * @param ctx the parse tree
     */
    void exitStart(ParseRuleParser.StartContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#semRel}.
     *
     * @param ctx the parse tree
     */
    void enterSemRel(ParseRuleParser.SemRelContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#semRel}.
     *
     * @param ctx the parse tree
     */
    void exitSemRel(ParseRuleParser.SemRelContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#semRelName}.
     *
     * @param ctx the parse tree
     */
    void enterSemRelName(ParseRuleParser.SemRelNameContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#semRelName}.
     *
     * @param ctx the parse tree
     */
    void exitSemRelName(ParseRuleParser.SemRelNameContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterExpression(ParseRuleParser.ExpressionContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitExpression(ParseRuleParser.ExpressionContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#rootNode}.
     *
     * @param ctx the parse tree
     */
    void enterRootNode(ParseRuleParser.RootNodeContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#rootNode}.
     *
     * @param ctx the parse tree
     */
    void exitRootNode(ParseRuleParser.RootNodeContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#leftExpression}.
     *
     * @param ctx the parse tree
     */
    void enterLeftExpression(ParseRuleParser.LeftExpressionContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#leftExpression}.
     *
     * @param ctx the parse tree
     */
    void exitLeftExpression(ParseRuleParser.LeftExpressionContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#rightExpression}.
     *
     * @param ctx the parse tree
     */
    void enterRightExpression(ParseRuleParser.RightExpressionContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#rightExpression}.
     *
     * @param ctx the parse tree
     */
    void exitRightExpression(ParseRuleParser.RightExpressionContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#leftEdge}.
     *
     * @param ctx the parse tree
     */
    void enterLeftEdge(ParseRuleParser.LeftEdgeContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#leftEdge}.
     *
     * @param ctx the parse tree
     */
    void exitLeftEdge(ParseRuleParser.LeftEdgeContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#rightEdge}.
     *
     * @param ctx the parse tree
     */
    void enterRightEdge(ParseRuleParser.RightEdgeContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#rightEdge}.
     *
     * @param ctx the parse tree
     */
    void exitRightEdge(ParseRuleParser.RightEdgeContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#depRel}.
     *
     * @param ctx the parse tree
     */
    void enterDepRel(ParseRuleParser.DepRelContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#depRel}.
     *
     * @param ctx the parse tree
     */
    void exitDepRel(ParseRuleParser.DepRelContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#depRelValue}.
     *
     * @param ctx the parse tree
     */
    void enterDepRelValue(ParseRuleParser.DepRelValueContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#depRelValue}.
     *
     * @param ctx the parse tree
     */
    void exitDepRelValue(ParseRuleParser.DepRelValueContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#node}.
     *
     * @param ctx the parse tree
     */
    void enterNode(ParseRuleParser.NodeContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#node}.
     *
     * @param ctx the parse tree
     */
    void exitNode(ParseRuleParser.NodeContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#xPos}.
     *
     * @param ctx the parse tree
     */
    void enterXPos(ParseRuleParser.XPosContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#xPos}.
     *
     * @param ctx the parse tree
     */
    void exitXPos(ParseRuleParser.XPosContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#xPosValue}.
     *
     * @param ctx the parse tree
     */
    void enterXPosValue(ParseRuleParser.XPosValueContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#xPosValue}.
     *
     * @param ctx the parse tree
     */
    void exitXPosValue(ParseRuleParser.XPosValueContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#element}.
     *
     * @param ctx the parse tree
     */
    void enterElement(ParseRuleParser.ElementContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#element}.
     *
     * @param ctx the parse tree
     */
    void exitElement(ParseRuleParser.ElementContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#namedEntityToRole}.
     *
     * @param ctx the parse tree
     */
    void enterNamedEntityToRole(ParseRuleParser.NamedEntityToRoleContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#namedEntityToRole}.
     *
     * @param ctx the parse tree
     */
    void exitNamedEntityToRole(ParseRuleParser.NamedEntityToRoleContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#namedEntity}.
     *
     * @param ctx the parse tree
     */
    void enterNamedEntity(ParseRuleParser.NamedEntityContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#namedEntity}.
     *
     * @param ctx the parse tree
     */
    void exitNamedEntity(ParseRuleParser.NamedEntityContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#role}.
     *
     * @param ctx the parse tree
     */
    void enterRole(ParseRuleParser.RoleContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#role}.
     *
     * @param ctx the parse tree
     */
    void exitRole(ParseRuleParser.RoleContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#text}.
     *
     * @param ctx the parse tree
     */
    void enterText(ParseRuleParser.TextContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#text}.
     *
     * @param ctx the parse tree
     */
    void exitText(ParseRuleParser.TextContext ctx);

    /**
     * Enter a parse tree produced by {@link ParseRuleParser#id}.
     *
     * @param ctx the parse tree
     */
    void enterId(ParseRuleParser.IdContext ctx);

    /**
     * Exit a parse tree produced by {@link ParseRuleParser#id}.
     *
     * @param ctx the parse tree
     */
    void exitId(ParseRuleParser.IdContext ctx);
}