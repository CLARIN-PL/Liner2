package g419.serel.parseRule;// Generated from /home/user57/NLPWR/projects/Liner2/g419-serel-core/src/main/antlr/ParseRule.g4 by ANTLR 4.8

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

/**
 * This class provides an empty implementation of {@link ParseRuleVisitor},
 * which can be extended to create a visitor which only needs to handle a subset
 * of the available methods.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public class ParseRuleBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements ParseRuleVisitor<T> {
  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitStart(final ParseRuleParser.StartContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitSemRel(final ParseRuleParser.SemRelContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitSemRelName(final ParseRuleParser.SemRelNameContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitExpression(final ParseRuleParser.ExpressionContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitRootNode(final ParseRuleParser.RootNodeContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitRootLeftExpression(final ParseRuleParser.RootLeftExpressionContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitRootRightExpression(final ParseRuleParser.RootRightExpressionContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitLeftExpression(final ParseRuleParser.LeftExpressionContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitRightExpression(final ParseRuleParser.RightExpressionContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitLeftEdge(final ParseRuleParser.LeftEdgeContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitRightEdge(final ParseRuleParser.RightEdgeContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitDepRel(final ParseRuleParser.DepRelContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitDepRelValue(final ParseRuleParser.DepRelValueContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitNode(final ParseRuleParser.NodeContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitXPos(final ParseRuleParser.XPosContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitXPosValue(final ParseRuleParser.XPosValueContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitElement(final ParseRuleParser.ElementContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitNamedEntityToRole(final ParseRuleParser.NamedEntityToRoleContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitToRole(final ParseRuleParser.ToRoleContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitNamedEntity(final ParseRuleParser.NamedEntityContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitRole(final ParseRuleParser.RoleContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitText(final ParseRuleParser.TextContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitFunctionName(final ParseRuleParser.FunctionNameContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitLemmas(final ParseRuleParser.LemmasContext ctx) { return visitChildren(ctx); }

  /**
   * {@inheritDoc}
   *
   * <p>The default implementation returns the result of calling
   * {@link #visitChildren} on {@code ctx}.</p>
   */
  @Override
  public T visitId(final ParseRuleParser.IdContext ctx) { return visitChildren(ctx); }
}