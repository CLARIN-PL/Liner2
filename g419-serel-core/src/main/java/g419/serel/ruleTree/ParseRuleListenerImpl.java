package g419.serel.ruleTree;

import g419.serel.parseRule.ParseRuleListener;
import g419.serel.parseRule.ParseRuleParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import static g419.serel.parseRule.ParseRuleParser.*;

//@Slf4j
public class ParseRuleListenerImpl implements ParseRuleListener {

  public NodeMatch rootNodeMatch;
  public String relationType;


  private EdgeMatch leftExpression;
  private EdgeMatch rightExpression;

  @Override
  public void enterStart(final ParseRuleParser.StartContext ctx) {
    //log.debug(" entering Start");
  }


  @Override
  public void exitStart(final StartContext ctx) {
    //log.debug(" exiting Start");
    if (leftExpression != null) {
      rootNodeMatch.edgeMatchList.add(leftExpression);
    }
    if (rightExpression != null) {
      rootNodeMatch.edgeMatchList.add(rightExpression);
    }


    //rootNodeMatch.dumpString();
  }

  @Override
  public void exitRootNode(final ParseRuleParser.RootNodeContext ctx) {
    //log.debug(" exiting RootNode");

    rootNodeMatch = rootNodeContext2NodeMatch(ctx);

  }

  private NodeMatch rootNodeContext2NodeMatch(final RootNodeContext ctx) {
    final NodeContext nCtx = ctx.node();
    return nodeContext2NodeMatch(nCtx);
  }


  @Override
  public void exitSemRelName(final ParseRuleParser.SemRelNameContext ctx) {
    //log.debug(" exiting SemRelName");
    relationType = ctx.getText();
  }


  @Override
  public void exitLeftExpression(final ParseRuleParser.LeftExpressionContext ctx) {
    //log.debug(" exiting LeftExpression");
    leftExpression = leftExpression2EdgeMatch(ctx);
  }

  private EdgeMatch leftExpression2EdgeMatch(final LeftExpressionContext ctx) {
    //log.debug("leftExpression2EdgeMatch invoked ctx = " + ctx);

    final EdgeMatch edgeMatch = leftEdgeContext2EdgeMatch(ctx.leftEdge());
    final NodeMatch nodeMatch = nodeContext2NodeMatch(ctx.node());
    edgeMatch.nodeMatch = nodeMatch;

    if (ctx.leftExpression() != null) {
      nodeMatch.edgeMatchList.add(leftExpression2EdgeMatch(ctx.leftExpression()));
    }
    return edgeMatch;
  }


  private EdgeMatch leftEdgeContext2EdgeMatch(final LeftEdgeContext ctx) {

    final EdgeMatch edgeMatch = new EdgeMatch();
    edgeMatch.setSide("left");
    if (ctx.depRel() != null) {
      edgeMatch.setDepRel(ctx.depRel().depRelValue().getText());
    }

    return edgeMatch;
  }

  private NodeMatch nodeContext2NodeMatch(final NodeContext ctx) {
    final NodeMatch nodeMatch = new NodeMatch();
    if (ctx != null) {
      if (ctx.element() != null) {

        final String text = ctx.element().text().getText();
        if (text.equals("*")) {
          nodeMatch.setMatchAny(true);
        } else if (text.charAt(0) == '^') {
          nodeMatch.setMatchLemma(true);
          nodeMatch.setText(text.substring(1));
        } else {
          nodeMatch.setText(text);
        }

        if (ctx.element().namedEntityToRole() != null) {
          nodeMatch.setNamedEntity(ctx.element().namedEntityToRole().namedEntity().getText());
          nodeMatch.setRole(ctx.element().namedEntityToRole().role().getText());
        }
      }
      if (ctx.xPos() != null) {
        nodeMatch.setXPos(ctx.xPos().xPosValue().getText());
      }
    }

    return nodeMatch;
  }


  @Override
  public void exitRightExpression(final ParseRuleParser.RightExpressionContext ctx) {
    //log.debug(" exiting RightExpression");
    rightExpression = rightExpression2EdgeMatch(ctx);
  }


  private EdgeMatch rightExpression2EdgeMatch(final RightExpressionContext ctx) {
    //log.debug("rightExpression2EdgeMatch invoked ctx = " + ctx);

    final EdgeMatch edgeMatch = rightEdgeContext2EdgeMatch(ctx.rightEdge());
    final NodeMatch nodeMatch = nodeContext2NodeMatch(ctx.node());
    edgeMatch.nodeMatch = nodeMatch;

    if (ctx.rightExpression() != null) {
      nodeMatch.edgeMatchList.add(rightExpression2EdgeMatch(ctx.rightExpression()));
    }
    return edgeMatch;
  }

  private EdgeMatch rightEdgeContext2EdgeMatch(final RightEdgeContext ctx) {

    final EdgeMatch edgeMatch = new EdgeMatch();
    edgeMatch.setSide("right");
    if (ctx != null) {
      if (ctx.depRel() != null) {
        edgeMatch.setDepRel(ctx.depRel().depRelValue().getText());
      }
    }

    return edgeMatch;
  }




  /*
   *     *************************************************************************************************************************
   *     *************************************************************************************************************************
   *     *************************************************************************************************************************
   *     *************************************************************************************************************************
   *     *************************************************************************************************************************
   */


  @Override
  public void enterSemRel(final ParseRuleParser.SemRelContext ctx) {

  }

  @Override
  public void exitSemRel(final ParseRuleParser.SemRelContext ctx) {

  }

  @Override
  public void enterExpression(final ParseRuleParser.ExpressionContext ctx) {
    //log.debug(" entering Expression");

  }

  @Override
  public void exitExpression(final ParseRuleParser.ExpressionContext ctx) {
    //log.debug(" exiting Expression");

  }

  @Override
  public void enterRootNode(final ParseRuleParser.RootNodeContext ctx) {
    //log.debug(" entering RootNode");
  }


  @Override
  public void enterSemRelName(final ParseRuleParser.SemRelNameContext ctx) {
    //log.debug(" entering SemRelName");

  }


  @Override
  public void enterRightExpression(final ParseRuleParser.RightExpressionContext ctx) {
    //log.debug(" entering RightExpression");

  }


  @Override
  public void enterDepRelValue(final ParseRuleParser.DepRelValueContext ctx) {
    //log.debug(" entering DepRelValue");

  }

  @Override
  public void exitDepRelValue(final ParseRuleParser.DepRelValueContext ctx) {
    //log.debug(" exiting DepRelValue");

  }

  @Override
  public void enterDepRel(final ParseRuleParser.DepRelContext ctx) {
    //log.debug(" entering DepRel");

  }

  @Override
  public void exitDepRel(final ParseRuleParser.DepRelContext ctx) {
    //log.debug(" exiting DepRel");

  }

  @Override
  public void enterLeftEdge(final ParseRuleParser.LeftEdgeContext ctx) {
    //log.debug(" entering LeftEdge");

  }

  @Override
  public void exitLeftEdge(final ParseRuleParser.LeftEdgeContext ctx) {
    //log.debug(" exiting LeftEdge");

  }

  @Override
  public void enterRightEdge(final ParseRuleParser.RightEdgeContext ctx) {
    //log.debug(" entering RightEdge");

  }

  @Override
  public void exitRightEdge(final ParseRuleParser.RightEdgeContext ctx) {
    //log.debug(" exiting RightEdge");

  }

  @Override
  public void enterNamedEntity(final ParseRuleParser.NamedEntityContext ctx) {
    //log.debug(" entering NamedEntity");

  }

  @Override
  public void exitNamedEntity(final ParseRuleParser.NamedEntityContext ctx) {
    //log.debug(" exiting NamedEntity");

  }

  @Override
  public void enterRole(final ParseRuleParser.RoleContext ctx) {
    //log.debug(" entering Role");

  }

  @Override
  public void exitRole(final ParseRuleParser.RoleContext ctx) {
    //log.debug(" exiting Role");

  }

  @Override
  public void enterText(final ParseRuleParser.TextContext ctx) {
    //log.debug(" entering Text");

  }

  @Override
  public void exitText(final ParseRuleParser.TextContext ctx) {
    //log.debug(" exiting Text");

  }

  @Override
  public void enterNamedEntityToRole(final ParseRuleParser.NamedEntityToRoleContext ctx) {
    //log.debug(" entering NamedEntityToRole");

  }

  @Override
  public void exitNamedEntityToRole(final ParseRuleParser.NamedEntityToRoleContext ctx) {
    //log.debug(" exiting NamedEntityToRole");

  }

  @Override
  public void enterElement(final ParseRuleParser.ElementContext ctx) {
    //log.debug(" entering Element");

  }

  @Override
  public void exitElement(final ParseRuleParser.ElementContext ctx) {
    //log.debug(" exiting Element");

  }

  @Override
  public void enterXPosValue(final ParseRuleParser.XPosValueContext ctx) {
    //log.debug(" entering XPosValue");

  }

  @Override
  public void exitXPosValue(final ParseRuleParser.XPosValueContext ctx) {
    //log.debug(" exiting XPosValue");

  }

  @Override
  public void enterXPos(final ParseRuleParser.XPosContext ctx) {
    //log.debug(" entering XPos");

  }

  @Override
  public void exitXPos(final ParseRuleParser.XPosContext ctx) {
    //log.debug(" exiting XPos");

  }

  @Override
  public void enterNode(final ParseRuleParser.NodeContext ctx) {
    //log.debug(" entering Node");
  }

  @Override
  public void exitNode(final ParseRuleParser.NodeContext ctx) {
    //log.debug(" exiting Node");

  }


  @Override
  public void enterLeftExpression(final ParseRuleParser.LeftExpressionContext ctx) {
    //log.debug(" entering LeftExpression");
  }


  @Override
  public void enterId(final ParseRuleParser.IdContext ctx) {
    ////log.debug(" entering Id");

  }

  @Override
  public void exitId(final ParseRuleParser.IdContext ctx) {
    ////log.debug(" exiting Id");

  }

  @Override
  public void visitTerminal(final TerminalNode terminalNode) {
    ////log.debug(" visiting  Terminal");
  }

  @Override
  public void visitErrorNode(final ErrorNode errorNode) {

  }

  @Override
  public void enterEveryRule(final ParserRuleContext parserRuleContext) {
    ////log.debug(" entering EveryRule");

  }

  @Override
  public void exitEveryRule(final ParserRuleContext parserRuleContext) {
    ////log.debug(" exiting EveryRule");

  }


}






