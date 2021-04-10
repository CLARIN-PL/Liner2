package g419.serel.parseRule;// Generated from /home/user57/NLPWR/projects/Liner2/g419-serel-core/src/main/antlr/ParseRule.g4 by ANTLR 4.8

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ParseRuleParser extends Parser {
  static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

  protected static final DFA[] _decisionToDFA;
  protected static final PredictionContextCache _sharedContextCache =
      new PredictionContextCache();
  public static final int
      T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
      T__9 = 10, STAR = 11, LEMMA = 12, HASH = 13, IDENTIFIER = 14, WS = 15;
  public static final int
      RULE_start = 0, RULE_semRel = 1, RULE_semRelName = 2, RULE_expression = 3,
      RULE_rootNode = 4, RULE_rootLeftExpression = 5, RULE_rootRightExpression = 6,
      RULE_leftExpression = 7, RULE_rightExpression = 8, RULE_leftEdge = 9,
      RULE_rightEdge = 10, RULE_depRel = 11, RULE_depRelValue = 12, RULE_depRelValuePart = 13,
      RULE_node = 14, RULE_xPos = 15, RULE_xPosValue = 16, RULE_element = 17,
      RULE_namedEntityToRole = 18, RULE_toRole = 19, RULE_namedEntity = 20,
      RULE_role = 21, RULE_text = 22, RULE_id = 23, RULE_lemmas = 24, RULE_functions = 25,
      RULE_functionName = 26, RULE_caseTail = 27;

  private static String[] makeRuleNames() {
    return new String[]{
        "start", "semRel", "semRelName", "expression", "rootNode", "rootLeftExpression",
        "rootRightExpression", "leftExpression", "rightExpression", "leftEdge",
        "rightEdge", "depRel", "depRelValue", "depRelValuePart", "node", "xPos",
        "xPosValue", "element", "namedEntityToRole", "toRole", "namedEntity",
        "role", "text", "id", "lemmas", "functions", "functionName", "caseTail"
    };
  }

  public static final String[] ruleNames = makeRuleNames();

  private static String[] makeLiteralNames() {
    return new String[]{
        null, "'::'", "'>'", "'<'", "'('", "')'", "':'", "'['", "']'", "'/'",
        "'|'", "'*'", "'^'", "'#'"
    };
  }

  private static final String[] _LITERAL_NAMES = makeLiteralNames();

  private static String[] makeSymbolicNames() {
    return new String[]{
        null, null, null, null, null, null, null, null, null, null, null, "STAR",
        "LEMMA", "HASH", "IDENTIFIER", "WS"
    };
  }

  private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
  public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

  /**
   * @deprecated Use {@link #VOCABULARY} instead.
   */
  @Deprecated
  public static final String[] tokenNames;

  static {
    tokenNames = new String[_SYMBOLIC_NAMES.length];
    for (int i = 0; i < tokenNames.length; i++) {
      tokenNames[i] = VOCABULARY.getLiteralName(i);
      if (tokenNames[i] == null) {
        tokenNames[i] = VOCABULARY.getSymbolicName(i);
      }

      if (tokenNames[i] == null) {
        tokenNames[i] = "<INVALID>";
      }
    }
  }

  @Override
  @Deprecated
  public String[] getTokenNames() {
    return tokenNames;
  }

  @Override

  public Vocabulary getVocabulary() {
    return VOCABULARY;
  }

  @Override
  public String getGrammarFileName() { return "ParseRule.g4"; }

  @Override
  public String[] getRuleNames() { return ruleNames; }

  @Override
  public String getSerializedATN() { return _serializedATN; }

  @Override
  public ATN getATN() { return _ATN; }

  public ParseRuleParser(TokenStream input) {
    super(input);
    _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  public static class StartContext extends ParserRuleContext {
    public ExpressionContext expression() {
      return getRuleContext(ExpressionContext.class, 0);
    }

    public TerminalNode EOF() { return getToken(ParseRuleParser.EOF, 0); }

    public SemRelContext semRel() {
      return getRuleContext(SemRelContext.class, 0);
    }

    public StartContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_start; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterStart(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitStart(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitStart(this);
      else return visitor.visitChildren(this);
    }
  }

  public final StartContext start() throws RecognitionException {
    StartContext _localctx = new StartContext(_ctx, getState());
    enterRule(_localctx, 0, RULE_start);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(57);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
          case 1: {
            setState(56);
            semRel();
          }
          break;
        }
        setState(59);
        expression();
        setState(60);
        match(EOF);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class SemRelContext extends ParserRuleContext {
    public SemRelNameContext semRelName() {
      return getRuleContext(SemRelNameContext.class, 0);
    }

    public SemRelContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_semRel; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterSemRel(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitSemRel(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitSemRel(this);
      else return visitor.visitChildren(this);
    }
  }

  public final SemRelContext semRel() throws RecognitionException {
    SemRelContext _localctx = new SemRelContext(_ctx, getState());
    enterRule(_localctx, 2, RULE_semRel);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(62);
        semRelName();
        setState(63);
        match(T__0);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class SemRelNameContext extends ParserRuleContext {
    public IdContext id() {
      return getRuleContext(IdContext.class, 0);
    }

    public SemRelNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_semRelName; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterSemRelName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitSemRelName(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitSemRelName(this);
      else return visitor.visitChildren(this);
    }
  }

  public final SemRelNameContext semRelName() throws RecognitionException {
    SemRelNameContext _localctx = new SemRelNameContext(_ctx, getState());
    enterRule(_localctx, 4, RULE_semRelName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(65);
        id();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class ExpressionContext extends ParserRuleContext {
    public RootNodeContext rootNode() {
      return getRuleContext(RootNodeContext.class, 0);
    }

    public RootLeftExpressionContext rootLeftExpression() {
      return getRuleContext(RootLeftExpressionContext.class, 0);
    }

    public RootRightExpressionContext rootRightExpression() {
      return getRuleContext(RootRightExpressionContext.class, 0);
    }

    public ExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_expression; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitExpression(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitExpression(this);
      else return visitor.visitChildren(this);
    }
  }

  public final ExpressionContext expression() throws RecognitionException {
    ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
    enterRule(_localctx, 6, RULE_expression);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(68);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 1, _ctx)) {
          case 1: {
            setState(67);
            rootLeftExpression();
          }
          break;
        }
        setState(70);
        rootNode();
        setState(72);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__2) {
          {
            setState(71);
            rootRightExpression();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class RootNodeContext extends ParserRuleContext {
    public NodeContext node() {
      return getRuleContext(NodeContext.class, 0);
    }

    public RootNodeContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_rootNode; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRootNode(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRootNode(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitRootNode(this);
      else return visitor.visitChildren(this);
    }
  }

  public final RootNodeContext rootNode() throws RecognitionException {
    RootNodeContext _localctx = new RootNodeContext(_ctx, getState());
    enterRule(_localctx, 8, RULE_rootNode);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(74);
        node();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class RootLeftExpressionContext extends ParserRuleContext {
    public LeftExpressionContext leftExpression() {
      return getRuleContext(LeftExpressionContext.class, 0);
    }

    public RootLeftExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_rootLeftExpression; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRootLeftExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRootLeftExpression(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor)
        return ((ParseRuleVisitor<? extends T>) visitor).visitRootLeftExpression(this);
      else return visitor.visitChildren(this);
    }
  }

  public final RootLeftExpressionContext rootLeftExpression() throws RecognitionException {
    RootLeftExpressionContext _localctx = new RootLeftExpressionContext(_ctx, getState());
    enterRule(_localctx, 10, RULE_rootLeftExpression);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(76);
        leftExpression(0);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class RootRightExpressionContext extends ParserRuleContext {
    public RightExpressionContext rightExpression() {
      return getRuleContext(RightExpressionContext.class, 0);
    }

    public RootRightExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_rootRightExpression; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRootRightExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRootRightExpression(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor)
        return ((ParseRuleVisitor<? extends T>) visitor).visitRootRightExpression(this);
      else return visitor.visitChildren(this);
    }
  }

  public final RootRightExpressionContext rootRightExpression() throws RecognitionException {
    RootRightExpressionContext _localctx = new RootRightExpressionContext(_ctx, getState());
    enterRule(_localctx, 12, RULE_rootRightExpression);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(78);
        rightExpression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class LeftExpressionContext extends ParserRuleContext {
    public NodeContext node() {
      return getRuleContext(NodeContext.class, 0);
    }

    public LeftEdgeContext leftEdge() {
      return getRuleContext(LeftEdgeContext.class, 0);
    }

    public LeftExpressionContext leftExpression() {
      return getRuleContext(LeftExpressionContext.class, 0);
    }

    public LeftExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_leftExpression; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterLeftExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitLeftExpression(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor)
        return ((ParseRuleVisitor<? extends T>) visitor).visitLeftExpression(this);
      else return visitor.visitChildren(this);
    }
  }

  public final LeftExpressionContext leftExpression() throws RecognitionException {
    return leftExpression(0);
  }

  private LeftExpressionContext leftExpression(int _p) throws RecognitionException {
    ParserRuleContext _parentctx = _ctx;
    int _parentState = getState();
    LeftExpressionContext _localctx = new LeftExpressionContext(_ctx, _parentState);
    LeftExpressionContext _prevctx = _localctx;
    int _startState = 14;
    enterRecursionRule(_localctx, 14, RULE_leftExpression, _p);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        {
          setState(81);
          node();
          setState(82);
          leftEdge();
        }
        _ctx.stop = _input.LT(-1);
        setState(90);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
        while (_alt != 2 && _alt != ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            if (_parseListeners != null) triggerExitRuleEvent();
            _prevctx = _localctx;
            {
              {
                _localctx = new LeftExpressionContext(_parentctx, _parentState);
                pushNewRecursionContext(_localctx, _startState, RULE_leftExpression);
                setState(84);
                if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                setState(85);
                node();
                setState(86);
                leftEdge();
              }
            }
          }
          setState(92);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      unrollRecursionContexts(_parentctx);
    }
    return _localctx;
  }

  public static class RightExpressionContext extends ParserRuleContext {
    public RightEdgeContext rightEdge() {
      return getRuleContext(RightEdgeContext.class, 0);
    }

    public NodeContext node() {
      return getRuleContext(NodeContext.class, 0);
    }

    public RightExpressionContext rightExpression() {
      return getRuleContext(RightExpressionContext.class, 0);
    }

    public RightExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_rightExpression; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRightExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRightExpression(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor)
        return ((ParseRuleVisitor<? extends T>) visitor).visitRightExpression(this);
      else return visitor.visitChildren(this);
    }
  }

  public final RightExpressionContext rightExpression() throws RecognitionException {
    RightExpressionContext _localctx = new RightExpressionContext(_ctx, getState());
    enterRule(_localctx, 16, RULE_rightExpression);
    try {
      setState(100);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 4, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(93);
          rightEdge();
          setState(94);
          node();
          setState(95);
          rightExpression();
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(97);
          rightEdge();
          setState(98);
          node();
        }
        break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class LeftEdgeContext extends ParserRuleContext {
    public DepRelContext depRel() {
      return getRuleContext(DepRelContext.class, 0);
    }

    public LeftEdgeContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_leftEdge; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterLeftEdge(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitLeftEdge(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitLeftEdge(this);
      else return visitor.visitChildren(this);
    }
  }

  public final LeftEdgeContext leftEdge() throws RecognitionException {
    LeftEdgeContext _localctx = new LeftEdgeContext(_ctx, getState());
    enterRule(_localctx, 18, RULE_leftEdge);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(103);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__3) {
          {
            setState(102);
            depRel();
          }
        }

        setState(105);
        match(T__1);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class RightEdgeContext extends ParserRuleContext {
    public DepRelContext depRel() {
      return getRuleContext(DepRelContext.class, 0);
    }

    public RightEdgeContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_rightEdge; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRightEdge(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRightEdge(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitRightEdge(this);
      else return visitor.visitChildren(this);
    }
  }

  public final RightEdgeContext rightEdge() throws RecognitionException {
    RightEdgeContext _localctx = new RightEdgeContext(_ctx, getState());
    enterRule(_localctx, 20, RULE_rightEdge);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(107);
        match(T__2);
        setState(109);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__3) {
          {
            setState(108);
            depRel();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class DepRelContext extends ParserRuleContext {
    public DepRelValueContext depRelValue() {
      return getRuleContext(DepRelValueContext.class, 0);
    }

    public DepRelContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_depRel; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterDepRel(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitDepRel(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitDepRel(this);
      else return visitor.visitChildren(this);
    }
  }

  public final DepRelContext depRel() throws RecognitionException {
    DepRelContext _localctx = new DepRelContext(_ctx, getState());
    enterRule(_localctx, 22, RULE_depRel);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(111);
        match(T__3);
        setState(112);
        depRelValue();
        setState(113);
        match(T__4);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class DepRelValueContext extends ParserRuleContext {
    public List<DepRelValuePartContext> depRelValuePart() {
      return getRuleContexts(DepRelValuePartContext.class);
    }

    public DepRelValuePartContext depRelValuePart(int i) {
      return getRuleContext(DepRelValuePartContext.class, i);
    }

    public DepRelValueContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_depRelValue; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterDepRelValue(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitDepRelValue(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitDepRelValue(this);
      else return visitor.visitChildren(this);
    }
  }

  public final DepRelValueContext depRelValue() throws RecognitionException {
    DepRelValueContext _localctx = new DepRelValueContext(_ctx, getState());
    enterRule(_localctx, 24, RULE_depRelValue);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(115);
        depRelValuePart();
        setState(120);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__5) {
          {
            {
              setState(116);
              match(T__5);
              setState(117);
              depRelValuePart();
            }
          }
          setState(122);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class DepRelValuePartContext extends ParserRuleContext {
    public TerminalNode IDENTIFIER() { return getToken(ParseRuleParser.IDENTIFIER, 0); }

    public DepRelValuePartContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_depRelValuePart; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterDepRelValuePart(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitDepRelValuePart(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor)
        return ((ParseRuleVisitor<? extends T>) visitor).visitDepRelValuePart(this);
      else return visitor.visitChildren(this);
    }
  }

  public final DepRelValuePartContext depRelValuePart() throws RecognitionException {
    DepRelValuePartContext _localctx = new DepRelValuePartContext(_ctx, getState());
    enterRule(_localctx, 26, RULE_depRelValuePart);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(123);
        match(IDENTIFIER);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class NodeContext extends ParserRuleContext {
    public ElementContext element() {
      return getRuleContext(ElementContext.class, 0);
    }

    public XPosContext xPos() {
      return getRuleContext(XPosContext.class, 0);
    }

    public NodeContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_node; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterNode(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitNode(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitNode(this);
      else return visitor.visitChildren(this);
    }
  }

  public final NodeContext node() throws RecognitionException {
    NodeContext _localctx = new NodeContext(_ctx, getState());
    enterRule(_localctx, 28, RULE_node);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(126);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__6) {
          {
            setState(125);
            xPos();
          }
        }

        setState(128);
        element();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class XPosContext extends ParserRuleContext {
    public XPosValueContext xPosValue() {
      return getRuleContext(XPosValueContext.class, 0);
    }

    public XPosContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_xPos; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterXPos(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitXPos(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitXPos(this);
      else return visitor.visitChildren(this);
    }
  }

  public final XPosContext xPos() throws RecognitionException {
    XPosContext _localctx = new XPosContext(_ctx, getState());
    enterRule(_localctx, 30, RULE_xPos);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(130);
        match(T__6);
        setState(131);
        xPosValue();
        setState(132);
        match(T__7);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class XPosValueContext extends ParserRuleContext {
    public IdContext id() {
      return getRuleContext(IdContext.class, 0);
    }

    public XPosValueContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_xPosValue; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterXPosValue(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitXPosValue(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitXPosValue(this);
      else return visitor.visitChildren(this);
    }
  }

  public final XPosValueContext xPosValue() throws RecognitionException {
    XPosValueContext _localctx = new XPosValueContext(_ctx, getState());
    enterRule(_localctx, 32, RULE_xPosValue);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(134);
        id();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class ElementContext extends ParserRuleContext {
    public TextContext text() {
      return getRuleContext(TextContext.class, 0);
    }

    public TerminalNode HASH() { return getToken(ParseRuleParser.HASH, 0); }

    public CaseTailContext caseTail() {
      return getRuleContext(CaseTailContext.class, 0);
    }

    public NamedEntityToRoleContext namedEntityToRole() {
      return getRuleContext(NamedEntityToRoleContext.class, 0);
    }

    public ElementContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_element; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterElement(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitElement(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitElement(this);
      else return visitor.visitChildren(this);
    }
  }

  public final ElementContext element() throws RecognitionException {
    ElementContext _localctx = new ElementContext(_ctx, getState());
    enterRule(_localctx, 34, RULE_element);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(136);
        text();
        setState(139);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == HASH) {
          {
            setState(137);
            match(HASH);
            setState(138);
            caseTail();
          }
        }

        setState(143);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__8) {
          {
            setState(141);
            match(T__8);
            setState(142);
            namedEntityToRole();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class NamedEntityToRoleContext extends ParserRuleContext {
    public NamedEntityContext namedEntity() {
      return getRuleContext(NamedEntityContext.class, 0);
    }

    public ToRoleContext toRole() {
      return getRuleContext(ToRoleContext.class, 0);
    }

    public NamedEntityToRoleContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_namedEntityToRole; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterNamedEntityToRole(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitNamedEntityToRole(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor)
        return ((ParseRuleVisitor<? extends T>) visitor).visitNamedEntityToRole(this);
      else return visitor.visitChildren(this);
    }
  }

  public final NamedEntityToRoleContext namedEntityToRole() throws RecognitionException {
    NamedEntityToRoleContext _localctx = new NamedEntityToRoleContext(_ctx, getState());
    enterRule(_localctx, 36, RULE_namedEntityToRole);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(145);
        namedEntity();
        setState(147);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__5) {
          {
            setState(146);
            toRole();
          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class ToRoleContext extends ParserRuleContext {
    public RoleContext role() {
      return getRuleContext(RoleContext.class, 0);
    }

    public ToRoleContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_toRole; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterToRole(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitToRole(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitToRole(this);
      else return visitor.visitChildren(this);
    }
  }

  public final ToRoleContext toRole() throws RecognitionException {
    ToRoleContext _localctx = new ToRoleContext(_ctx, getState());
    enterRule(_localctx, 38, RULE_toRole);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(149);
        match(T__5);
        setState(150);
        role();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class NamedEntityContext extends ParserRuleContext {
    public IdContext id() {
      return getRuleContext(IdContext.class, 0);
    }

    public NamedEntityContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_namedEntity; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterNamedEntity(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitNamedEntity(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitNamedEntity(this);
      else return visitor.visitChildren(this);
    }
  }

  public final NamedEntityContext namedEntity() throws RecognitionException {
    NamedEntityContext _localctx = new NamedEntityContext(_ctx, getState());
    enterRule(_localctx, 40, RULE_namedEntity);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(152);
        id();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class RoleContext extends ParserRuleContext {
    public IdContext id() {
      return getRuleContext(IdContext.class, 0);
    }

    public RoleContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_role; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRole(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRole(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitRole(this);
      else return visitor.visitChildren(this);
    }
  }

  public final RoleContext role() throws RecognitionException {
    RoleContext _localctx = new RoleContext(_ctx, getState());
    enterRule(_localctx, 42, RULE_role);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(154);
        id();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class TextContext extends ParserRuleContext {
    public IdContext id() {
      return getRuleContext(IdContext.class, 0);
    }

    public TextContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_text; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterText(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitText(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitText(this);
      else return visitor.visitChildren(this);
    }
  }

  public final TextContext text() throws RecognitionException {
    TextContext _localctx = new TextContext(_ctx, getState());
    enterRule(_localctx, 44, RULE_text);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(156);
        id();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class IdContext extends ParserRuleContext {
    public TerminalNode STAR() { return getToken(ParseRuleParser.STAR, 0); }

    public LemmasContext lemmas() {
      return getRuleContext(LemmasContext.class, 0);
    }

    public TerminalNode LEMMA() { return getToken(ParseRuleParser.LEMMA, 0); }

    public FunctionsContext functions() {
      return getRuleContext(FunctionsContext.class, 0);
    }

    public IdContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_id; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterId(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitId(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitId(this);
      else return visitor.visitChildren(this);
    }
  }

  public final IdContext id() throws RecognitionException {
    IdContext _localctx = new IdContext(_ctx, getState());
    enterRule(_localctx, 46, RULE_id);
    int _la;
    try {
      setState(166);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case STAR:
          enterOuterAlt(_localctx, 1);
        {
          setState(158);
          match(STAR);
        }
        break;
        case LEMMA:
        case IDENTIFIER:
          enterOuterAlt(_localctx, 2);
        {
          setState(163);
          _errHandler.sync(this);
          _la = _input.LA(1);
          if (_la == LEMMA) {
            {
              setState(159);
              match(LEMMA);
              setState(161);
              _errHandler.sync(this);
              switch (getInterpreter().adaptivePredict(_input, 12, _ctx)) {
                case 1: {
                  setState(160);
                  functions();
                }
                break;
              }
            }
          }

          setState(165);
          lemmas();
        }
        break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class LemmasContext extends ParserRuleContext {
    public List<TerminalNode> IDENTIFIER() { return getTokens(ParseRuleParser.IDENTIFIER); }

    public TerminalNode IDENTIFIER(int i) {
      return getToken(ParseRuleParser.IDENTIFIER, i);
    }

    public LemmasContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_lemmas; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterLemmas(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitLemmas(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitLemmas(this);
      else return visitor.visitChildren(this);
    }
  }

  public final LemmasContext lemmas() throws RecognitionException {
    LemmasContext _localctx = new LemmasContext(_ctx, getState());
    enterRule(_localctx, 48, RULE_lemmas);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(168);
        match(IDENTIFIER);
        setState(173);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__9) {
          {
            {
              setState(169);
              match(T__9);
              setState(170);
              match(IDENTIFIER);
            }
          }
          setState(175);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class FunctionsContext extends ParserRuleContext {
    public List<FunctionNameContext> functionName() {
      return getRuleContexts(FunctionNameContext.class);
    }

    public FunctionNameContext functionName(int i) {
      return getRuleContext(FunctionNameContext.class, i);
    }

    public FunctionsContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_functions; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterFunctions(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitFunctions(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitFunctions(this);
      else return visitor.visitChildren(this);
    }
  }

  public final FunctionsContext functions() throws RecognitionException {
    FunctionsContext _localctx = new FunctionsContext(_ctx, getState());
    enterRule(_localctx, 50, RULE_functions);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(179);
        _errHandler.sync(this);
        _alt = 1;
        do {
          switch (_alt) {
            case 1: {
              {
                setState(176);
                functionName();
                setState(177);
                match(T__5);
              }
            }
            break;
            default:
              throw new NoViableAltException(this);
          }
          setState(181);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 16, _ctx);
        } while (_alt != 2 && _alt != ATN.INVALID_ALT_NUMBER);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class FunctionNameContext extends ParserRuleContext {
    public TerminalNode IDENTIFIER() { return getToken(ParseRuleParser.IDENTIFIER, 0); }

    public FunctionNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_functionName; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterFunctionName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitFunctionName(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitFunctionName(this);
      else return visitor.visitChildren(this);
    }
  }

  public final FunctionNameContext functionName() throws RecognitionException {
    FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
    enterRule(_localctx, 52, RULE_functionName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(183);
        match(IDENTIFIER);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class CaseTailContext extends ParserRuleContext {
    public TerminalNode STAR() { return getToken(ParseRuleParser.STAR, 0); }

    public TerminalNode IDENTIFIER() { return getToken(ParseRuleParser.IDENTIFIER, 0); }

    public CaseTailContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_caseTail; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterCaseTail(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitCaseTail(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitCaseTail(this);
      else return visitor.visitChildren(this);
    }
  }

  public final CaseTailContext caseTail() throws RecognitionException {
    CaseTailContext _localctx = new CaseTailContext(_ctx, getState());
    enterRule(_localctx, 54, RULE_caseTail);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(185);
        _la = _input.LA(1);
        if (!(_la == STAR || _la == IDENTIFIER)) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF) matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
    switch (ruleIndex) {
      case 7:
        return leftExpression_sempred((LeftExpressionContext) _localctx, predIndex);
    }
    return true;
  }

  private boolean leftExpression_sempred(LeftExpressionContext _localctx, int predIndex) {
    switch (predIndex) {
      case 0:
        return precpred(_ctx, 2);
    }
    return true;
  }

  public static final String _serializedATN =
      "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\21\u00be\4\2\t\2" +
          "\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
          "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
          "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" +
          "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\3\2\5\2<\n\2\3\2\3\2\3\2\3\3" +
          "\3\3\3\3\3\4\3\4\3\5\5\5G\n\5\3\5\3\5\5\5K\n\5\3\6\3\6\3\7\3\7\3\b\3\b" +
          "\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\7\t[\n\t\f\t\16\t^\13\t\3\n\3\n\3\n\3" +
          "\n\3\n\3\n\3\n\5\ng\n\n\3\13\5\13j\n\13\3\13\3\13\3\f\3\f\5\fp\n\f\3\r" +
          "\3\r\3\r\3\r\3\16\3\16\3\16\7\16y\n\16\f\16\16\16|\13\16\3\17\3\17\3\20" +
          "\5\20\u0081\n\20\3\20\3\20\3\21\3\21\3\21\3\21\3\22\3\22\3\23\3\23\3\23" +
          "\5\23\u008e\n\23\3\23\3\23\5\23\u0092\n\23\3\24\3\24\5\24\u0096\n\24\3" +
          "\25\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\31\5\31\u00a4" +
          "\n\31\5\31\u00a6\n\31\3\31\5\31\u00a9\n\31\3\32\3\32\3\32\7\32\u00ae\n" +
          "\32\f\32\16\32\u00b1\13\32\3\33\3\33\3\33\6\33\u00b6\n\33\r\33\16\33\u00b7" +
          "\3\34\3\34\3\35\3\35\3\35\2\3\20\36\2\4\6\b\n\f\16\20\22\24\26\30\32\34" +
          "\36 \"$&(*,.\60\62\64\668\2\3\4\2\r\r\20\20\2\u00b2\2;\3\2\2\2\4@\3\2" +
          "\2\2\6C\3\2\2\2\bF\3\2\2\2\nL\3\2\2\2\fN\3\2\2\2\16P\3\2\2\2\20R\3\2\2" +
          "\2\22f\3\2\2\2\24i\3\2\2\2\26m\3\2\2\2\30q\3\2\2\2\32u\3\2\2\2\34}\3\2" +
          "\2\2\36\u0080\3\2\2\2 \u0084\3\2\2\2\"\u0088\3\2\2\2$\u008a\3\2\2\2&\u0093" +
          "\3\2\2\2(\u0097\3\2\2\2*\u009a\3\2\2\2,\u009c\3\2\2\2.\u009e\3\2\2\2\60" +
          "\u00a8\3\2\2\2\62\u00aa\3\2\2\2\64\u00b5\3\2\2\2\66\u00b9\3\2\2\28\u00bb" +
          "\3\2\2\2:<\5\4\3\2;:\3\2\2\2;<\3\2\2\2<=\3\2\2\2=>\5\b\5\2>?\7\2\2\3?" +
          "\3\3\2\2\2@A\5\6\4\2AB\7\3\2\2B\5\3\2\2\2CD\5\60\31\2D\7\3\2\2\2EG\5\f" +
          "\7\2FE\3\2\2\2FG\3\2\2\2GH\3\2\2\2HJ\5\n\6\2IK\5\16\b\2JI\3\2\2\2JK\3" +
          "\2\2\2K\t\3\2\2\2LM\5\36\20\2M\13\3\2\2\2NO\5\20\t\2O\r\3\2\2\2PQ\5\22" +
          "\n\2Q\17\3\2\2\2RS\b\t\1\2ST\5\36\20\2TU\5\24\13\2U\\\3\2\2\2VW\f\4\2" +
          "\2WX\5\36\20\2XY\5\24\13\2Y[\3\2\2\2ZV\3\2\2\2[^\3\2\2\2\\Z\3\2\2\2\\" +
          "]\3\2\2\2]\21\3\2\2\2^\\\3\2\2\2_`\5\26\f\2`a\5\36\20\2ab\5\22\n\2bg\3" +
          "\2\2\2cd\5\26\f\2de\5\36\20\2eg\3\2\2\2f_\3\2\2\2fc\3\2\2\2g\23\3\2\2" +
          "\2hj\5\30\r\2ih\3\2\2\2ij\3\2\2\2jk\3\2\2\2kl\7\4\2\2l\25\3\2\2\2mo\7" +
          "\5\2\2np\5\30\r\2on\3\2\2\2op\3\2\2\2p\27\3\2\2\2qr\7\6\2\2rs\5\32\16" +
          "\2st\7\7\2\2t\31\3\2\2\2uz\5\34\17\2vw\7\b\2\2wy\5\34\17\2xv\3\2\2\2y" +
          "|\3\2\2\2zx\3\2\2\2z{\3\2\2\2{\33\3\2\2\2|z\3\2\2\2}~\7\20\2\2~\35\3\2" +
          "\2\2\177\u0081\5 \21\2\u0080\177\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0082" +
          "\3\2\2\2\u0082\u0083\5$\23\2\u0083\37\3\2\2\2\u0084\u0085\7\t\2\2\u0085" +
          "\u0086\5\"\22\2\u0086\u0087\7\n\2\2\u0087!\3\2\2\2\u0088\u0089\5\60\31" +
          "\2\u0089#\3\2\2\2\u008a\u008d\5.\30\2\u008b\u008c\7\17\2\2\u008c\u008e" +
          "\58\35\2\u008d\u008b\3\2\2\2\u008d\u008e\3\2\2\2\u008e\u0091\3\2\2\2\u008f" +
          "\u0090\7\13\2\2\u0090\u0092\5&\24\2\u0091\u008f\3\2\2\2\u0091\u0092\3" +
          "\2\2\2\u0092%\3\2\2\2\u0093\u0095\5*\26\2\u0094\u0096\5(\25\2\u0095\u0094" +
          "\3\2\2\2\u0095\u0096\3\2\2\2\u0096\'\3\2\2\2\u0097\u0098\7\b\2\2\u0098" +
          "\u0099\5,\27\2\u0099)\3\2\2\2\u009a\u009b\5\60\31\2\u009b+\3\2\2\2\u009c" +
          "\u009d\5\60\31\2\u009d-\3\2\2\2\u009e\u009f\5\60\31\2\u009f/\3\2\2\2\u00a0" +
          "\u00a9\7\r\2\2\u00a1\u00a3\7\16\2\2\u00a2\u00a4\5\64\33\2\u00a3\u00a2" +
          "\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4\u00a6\3\2\2\2\u00a5\u00a1\3\2\2\2\u00a5" +
          "\u00a6\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7\u00a9\5\62\32\2\u00a8\u00a0\3" +
          "\2\2\2\u00a8\u00a5\3\2\2\2\u00a9\61\3\2\2\2\u00aa\u00af\7\20\2\2\u00ab" +
          "\u00ac\7\f\2\2\u00ac\u00ae\7\20\2\2\u00ad\u00ab\3\2\2\2\u00ae\u00b1\3" +
          "\2\2\2\u00af\u00ad\3\2\2\2\u00af\u00b0\3\2\2\2\u00b0\63\3\2\2\2\u00b1" +
          "\u00af\3\2\2\2\u00b2\u00b3\5\66\34\2\u00b3\u00b4\7\b\2\2\u00b4\u00b6\3" +
          "\2\2\2\u00b5\u00b2\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b7" +
          "\u00b8\3\2\2\2\u00b8\65\3\2\2\2\u00b9\u00ba\7\20\2\2\u00ba\67\3\2\2\2" +
          "\u00bb\u00bc\t\2\2\2\u00bc9\3\2\2\2\23;FJ\\fioz\u0080\u008d\u0091\u0095" +
          "\u00a3\u00a5\u00a8\u00af\u00b7";
  public static final ATN _ATN =
      new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}