package g419.serel.ruleTree.listeners;

import g419.serel.parseRule.ParseRuleListener;
import g419.serel.parseRule.ParseRuleParser;
import g419.serel.ruleTree.EdgeMatch;
import g419.serel.ruleTree.NodeMatch;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.LinkedList;
import java.util.List;

import static g419.serel.parseRule.ParseRuleParser.*;

@Slf4j
public class ParseRuleListenerImpl implements ParseRuleListener {

  private int nodeMatchIdSequence;
  public String relationType;

  public NodeMatch rootNodeMatch;
  private EdgeMatch rootLeftExpression;
  private EdgeMatch rootRightExpression;

  public List<NodeMatch> nodeMatchList = new LinkedList<>();

  @Override
  public void enterStart(final ParseRuleParser.StartContext ctx) {
    System.out.println(" entering Start");
    nodeMatchIdSequence = 1;
  }


  @Override
  public void exitStart(final StartContext ctx) {
    System.out.println(" exiting Start");
    if (rootLeftExpression != null) {
      rootNodeMatch.getEdgeMatchList().add(rootLeftExpression);
      rootLeftExpression.setParentNodeMatch(rootNodeMatch);
    }
    if (rootRightExpression != null) {
      rootNodeMatch.getEdgeMatchList().add(rootRightExpression);
      rootRightExpression.setParentNodeMatch(rootNodeMatch);
    }
    //rootNodeMatch.dumpString();
  }

  @Override
  public void exitRootNode(final ParseRuleParser.RootNodeContext ctx) {
    System.out.println(" exiting RootNode");
    rootNodeMatch = rootNodeContext2NodeMatch(ctx);
    System.out.println("created rootNodeatch >>> " + rootNodeMatch);
    rootNodeMatch.setParentEdgeMatch(null);
  }


  private NodeMatch rootNodeContext2NodeMatch(final RootNodeContext ctx) {
    final NodeContext nCtx = ctx.node();
    return nodeContext2NodeMatch(nCtx);
  }


  @Override
  public void exitSemRelName(final ParseRuleParser.SemRelNameContext ctx) {
    System.out.println(" exiting SemRelName");
    relationType = ctx.getText();
  }


  @Override
  public void exitRootLeftExpression(final RootLeftExpressionContext ctx) {
    rootLeftExpression = leftExpression2EdgeMatch(ctx.leftExpression());
  }

  @Override
  public void exitRootRightExpression(final RootRightExpressionContext ctx) {
    rootRightExpression = rightExpression2EdgeMatch(ctx.rightExpression());
  }


  private EdgeMatch leftExpression2EdgeMatch(final LeftExpressionContext ctx) {
    System.out.println("leftExpression2EdgeMatch invoked ctx = " + ctx);

    final EdgeMatch edgeMatch = leftEdgeContext2EdgeMatch(ctx.leftEdge());
    final NodeMatch nodeMatch = nodeContext2NodeMatch(ctx.node());
    edgeMatch.setNodeMatch(nodeMatch);
    nodeMatch.setParentEdgeMatch(edgeMatch);

    if (ctx.leftExpression() != null) {
      final EdgeMatch subLeftExpression = leftExpression2EdgeMatch(ctx.leftExpression());
      subLeftExpression.setParentNodeMatch(nodeMatch);
      nodeMatch.getEdgeMatchList().add(subLeftExpression);
    }
    return edgeMatch;
  }


  private EdgeMatch leftEdgeContext2EdgeMatch(final LeftEdgeContext ctx) {

    final EdgeMatch edgeMatch = new EdgeMatch();
    edgeMatch.setSide("left");
    if (ctx.depRel() != null) {
      final String text = ctx.depRel().depRelValue().getText();
      if (text.equals("*")) {
        edgeMatch.setMatchAnyDepRel(true);
      } else {
        edgeMatch.setDepRel(text);
      }
    } else {
      edgeMatch.setMatchAnyDepRel(true);
    }

    return edgeMatch;
  }

  private NodeMatch nodeContext2NodeMatch(final NodeContext ctx) {
    final NodeMatch nodeMatch = new NodeMatch();
    nodeMatch.setId(nodeMatchIdSequence++);
    nodeMatchList.add(nodeMatch);
    if (ctx != null) {
      if (ctx.element() != null) {

        final String text = ctx.element().text().getText();
        if (text.equals("*")) {
          nodeMatch.setMatchAnyText(true);
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


  private EdgeMatch rightExpression2EdgeMatch(final RightExpressionContext ctx) {
    System.out.println("rightExpression2EdgeMatch invoked ctx = " + ctx);

    final EdgeMatch edgeMatch = rightEdgeContext2EdgeMatch(ctx.rightEdge());
    System.out.println("created >>> " + edgeMatch);
    final NodeMatch nodeMatch = nodeContext2NodeMatch(ctx.node());
    System.out.println("created >>> " + nodeMatch);
    edgeMatch.setNodeMatch(nodeMatch);
    nodeMatch.setParentEdgeMatch(edgeMatch);

    if (ctx.rightExpression() != null) {
      final EdgeMatch subRightExpression = rightExpression2EdgeMatch(ctx.rightExpression());
      nodeMatch.getEdgeMatchList().add(subRightExpression);
      subRightExpression.setParentNodeMatch(nodeMatch);
    }
    return edgeMatch;
  }

  private EdgeMatch rightEdgeContext2EdgeMatch(final RightEdgeContext ctx) {

    final EdgeMatch edgeMatch = new EdgeMatch();
    edgeMatch.setSide("right");
    if (ctx != null) {
      if (ctx.depRel() != null) {
        final String text = ctx.depRel().depRelValue().getText();
        if (text.equals("*")) {
          edgeMatch.setMatchAnyDepRel(true);
        } else {
          edgeMatch.setDepRel(text);
        }
      } else {
        edgeMatch.setMatchAnyDepRel(true);
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
    System.out.println(" entering Expression");

  }

  @Override
  public void exitExpression(final ParseRuleParser.ExpressionContext ctx) {
    System.out.println(" exiting Expression");

  }

  @Override
  public void enterRootNode(final ParseRuleParser.RootNodeContext ctx) {
    System.out.println(" entering RootNode");
  }


  @Override
  public void enterSemRelName(final ParseRuleParser.SemRelNameContext ctx) {
    System.out.println(" entering SemRelName");

  }


  @Override
  public void enterRightExpression(final ParseRuleParser.RightExpressionContext ctx) {
    System.out.println(" entering RightExpression invoked ctx = " + ctx);

  }


  @Override
  public void enterDepRelValue(final ParseRuleParser.DepRelValueContext ctx) {
    System.out.println(" entering DepRelValue");

  }

  @Override
  public void exitDepRelValue(final ParseRuleParser.DepRelValueContext ctx) {
    System.out.println(" exiting DepRelValue");

  }

  @Override
  public void enterDepRel(final ParseRuleParser.DepRelContext ctx) {
    System.out.println(" entering DepRel");

  }

  @Override
  public void exitDepRel(final ParseRuleParser.DepRelContext ctx) {
    System.out.println(" exiting DepRel");

  }

  @Override
  public void enterLeftEdge(final ParseRuleParser.LeftEdgeContext ctx) {
    System.out.println(" entering LeftEdge");

  }

  @Override
  public void exitLeftEdge(final ParseRuleParser.LeftEdgeContext ctx) {
    System.out.println(" exiting LeftEdge");

  }

  @Override
  public void enterRightEdge(final ParseRuleParser.RightEdgeContext ctx) {
    System.out.println(" entering RightEdge");

  }

  @Override
  public void exitRightEdge(final ParseRuleParser.RightEdgeContext ctx) {
    System.out.println(" exiting RightEdge");

  }

  @Override
  public void enterNamedEntity(final ParseRuleParser.NamedEntityContext ctx) {
    System.out.println(" entering NamedEntity");

  }

  @Override
  public void exitNamedEntity(final ParseRuleParser.NamedEntityContext ctx) {
    System.out.println(" exiting NamedEntity");

  }

  @Override
  public void enterRole(final ParseRuleParser.RoleContext ctx) {
    System.out.println(" entering Role");

  }

  @Override
  public void exitRole(final ParseRuleParser.RoleContext ctx) {
    System.out.println(" exiting Role");

  }

  @Override
  public void enterText(final ParseRuleParser.TextContext ctx) {
    System.out.println(" entering Text");

  }

  @Override
  public void exitText(final ParseRuleParser.TextContext ctx) {
    System.out.println(" exiting Text");

  }

  @Override
  public void enterNamedEntityToRole(final ParseRuleParser.NamedEntityToRoleContext ctx) {
    System.out.println(" entering NamedEntityToRole");

  }

  @Override
  public void exitNamedEntityToRole(final ParseRuleParser.NamedEntityToRoleContext ctx) {
    System.out.println(" exiting NamedEntityToRole");

  }

  @Override
  public void enterElement(final ParseRuleParser.ElementContext ctx) {
    System.out.println(" entering Element");

  }

  @Override
  public void exitElement(final ParseRuleParser.ElementContext ctx) {
    System.out.println(" exiting Element");

  }

  @Override
  public void enterXPosValue(final ParseRuleParser.XPosValueContext ctx) {
    System.out.println(" entering XPosValue");

  }

  @Override
  public void exitXPosValue(final ParseRuleParser.XPosValueContext ctx) {
    System.out.println(" exiting XPosValue");

  }

  @Override
  public void enterXPos(final ParseRuleParser.XPosContext ctx) {
    System.out.println(" entering XPos");

  }

  @Override
  public void exitXPos(final ParseRuleParser.XPosContext ctx) {
    System.out.println(" exiting XPos");

  }

  @Override
  public void enterNode(final ParseRuleParser.NodeContext ctx) {
    System.out.println(" entering Node");
  }

  @Override
  public void exitNode(final ParseRuleParser.NodeContext ctx) {
    System.out.println(" exiting Node");

  }


  @Override
  public void enterLeftExpression(final ParseRuleParser.LeftExpressionContext ctx) {
    System.out.println(" entering LeftExpression");
  }


  @Override
  public void enterId(final ParseRuleParser.IdContext ctx) {
    //System.out.println(" entering Id");

  }

  @Override
  public void exitId(final ParseRuleParser.IdContext ctx) {
    //System.out.println(" exiting Id");

  }

  @Override
  public void visitTerminal(final TerminalNode terminalNode) {
    //System.out.println(" visiting  Terminal");
  }

  @Override
  public void visitErrorNode(final ErrorNode errorNode) {

  }

  @Override
  public void enterEveryRule(final ParserRuleContext parserRuleContext) {
    //System.out.println(" entering EveryRule");

  }

  @Override
  public void exitEveryRule(final ParserRuleContext parserRuleContext) {
    //System.out.println(" exiting EveryRule");

  }

  @Override
  public void exitLeftExpression(final ParseRuleParser.LeftExpressionContext ctx) {
    System.out.println(" exiting LeftExpression");
  }

  @Override
  public void exitRightExpression(final ParseRuleParser.RightExpressionContext ctx) {
    System.out.println(" exiting RightExpression");
  }

  @Override
  public void enterRootLeftExpression(final RootLeftExpressionContext ctx) {

  }

  @Override
  public void enterRootRightExpression(final RootRightExpressionContext ctx) {

  }


}






