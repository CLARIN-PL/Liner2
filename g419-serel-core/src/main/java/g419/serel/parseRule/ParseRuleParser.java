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
      T__9 = 10, T__10 = 11, T__11 = 12, STAR = 13, LEMMA = 14, HASH = 15, IDENTIFIER = 16,
      WS = 17;
  public static final int
      RULE_start = 0, RULE_semRel = 1, RULE_semRelName = 2, RULE_expression = 3,
      RULE_rootNode = 4, RULE_rootLeftExpression = 5, RULE_rootRightExpression = 6,
      RULE_leftExpression = 7, RULE_rightExpression = 8, RULE_leftEdge = 9,
      RULE_rightEdge = 10, RULE_depRel = 11, RULE_depRelValue = 12, RULE_depRelValuePart = 13,
      RULE_node = 14, RULE_uPos = 15, RULE_uPosValue = 16, RULE_xPos = 17, RULE_xPosValue = 18,
      RULE_xPosValuePart = 19, RULE_element = 20, RULE_namedEntityToRole = 21,
      RULE_toRole = 22, RULE_namedEntity = 23, RULE_role = 24, RULE_text = 25,
      RULE_id = 26, RULE_lemmas = 27, RULE_functions = 28, RULE_functionName = 29,
      RULE_caseTail = 30;

  private static String[] makeRuleNames() {
    return new String[]{
        "start", "semRel", "semRelName", "expression", "rootNode", "rootLeftExpression",
        "rootRightExpression", "leftExpression", "rightExpression", "leftEdge",
        "rightEdge", "depRel", "depRelValue", "depRelValuePart", "node", "uPos",
        "uPosValue", "xPos", "xPosValue", "xPosValuePart", "element", "namedEntityToRole",
        "toRole", "namedEntity", "role", "text", "id", "lemmas", "functions",
        "functionName", "caseTail"
    };
  }

  public static final String[] ruleNames = makeRuleNames();

  private static String[] makeLiteralNames() {
    return new String[]{
        null, "'::'", "'>'", "'<'", "'('", "')'", "':'", "'['", "']'", "'[['",
        "']]'", "'/'", "'|'", "'*'", "'^'", "'#'"
    };
  }

  private static final String[] _LITERAL_NAMES = makeLiteralNames();

  private static String[] makeSymbolicNames() {
    return new String[]{
        null, null, null, null, null, null, null, null, null, null, null, null,
        null, "STAR", "LEMMA", "HASH", "IDENTIFIER", "WS"
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
        setState(63);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
          case 1: {
            setState(62);
            semRel();
          }
          break;
        }
        setState(65);
        expression();
        setState(66);
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
        setState(68);
        semRelName();
        setState(69);
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
        setState(71);
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
        setState(74);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 1, _ctx)) {
          case 1: {
            setState(73);
            rootLeftExpression();
          }
          break;
        }
        setState(76);
        rootNode();
        setState(78);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__2) {
          {
            setState(77);
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
        setState(80);
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
        setState(82);
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
        setState(84);
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
          setState(87);
          node();
          setState(88);
          leftEdge();
        }
        _ctx.stop = _input.LT(-1);
        setState(96);
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
                setState(90);
                if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                setState(91);
                node();
                setState(92);
                leftEdge();
              }
            }
          }
          setState(98);
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
      setState(106);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 4, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1);
        {
          setState(99);
          rightEdge();
          setState(100);
          node();
          setState(101);
          rightExpression();
        }
        break;
        case 2:
          enterOuterAlt(_localctx, 2);
        {
          setState(103);
          rightEdge();
          setState(104);
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
        setState(109);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__3) {
          {
            setState(108);
            depRel();
          }
        }

        setState(111);
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
        setState(113);
        match(T__2);
        setState(115);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__3) {
          {
            setState(114);
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
        setState(117);
        match(T__3);
        setState(118);
        depRelValue();
        setState(119);
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
        setState(121);
        depRelValuePart();
        setState(126);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__5) {
          {
            {
              setState(122);
              match(T__5);
              setState(123);
              depRelValuePart();
            }
          }
          setState(128);
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
        setState(129);
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

    public UPosContext uPos() {
      return getRuleContext(UPosContext.class, 0);
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
        setState(132);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__6) {
          {
            setState(131);
            uPos();
          }
        }

        setState(135);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__8) {
          {
            setState(134);
            xPos();
          }
        }

        setState(137);
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

  public static class UPosContext extends ParserRuleContext {
    public UPosValueContext uPosValue() {
      return getRuleContext(UPosValueContext.class, 0);
    }

    public UPosContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_uPos; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterUPos(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitUPos(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitUPos(this);
      else return visitor.visitChildren(this);
    }
  }

  public final UPosContext uPos() throws RecognitionException {
    UPosContext _localctx = new UPosContext(_ctx, getState());
    enterRule(_localctx, 30, RULE_uPos);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(139);
        match(T__6);
        setState(140);
        uPosValue();
        setState(141);
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

  public static class UPosValueContext extends ParserRuleContext {
    public IdContext id() {
      return getRuleContext(IdContext.class, 0);
    }

    public UPosValueContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_uPosValue; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterUPosValue(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitUPosValue(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor) return ((ParseRuleVisitor<? extends T>) visitor).visitUPosValue(this);
      else return visitor.visitChildren(this);
    }
  }

  public final UPosValueContext uPosValue() throws RecognitionException {
    UPosValueContext _localctx = new UPosValueContext(_ctx, getState());
    enterRule(_localctx, 32, RULE_uPosValue);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(143);
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
    enterRule(_localctx, 34, RULE_xPos);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(145);
        match(T__8);
        setState(146);
        xPosValue();
        setState(147);
        match(T__9);
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
    public List<XPosValuePartContext> xPosValuePart() {
      return getRuleContexts(XPosValuePartContext.class);
    }

    public XPosValuePartContext xPosValuePart(int i) {
      return getRuleContext(XPosValuePartContext.class, i);
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
    enterRule(_localctx, 36, RULE_xPosValue);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(149);
        xPosValuePart();
        setState(154);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__5) {
          {
            {
              setState(150);
              match(T__5);
              setState(151);
              xPosValuePart();
            }
          }
          setState(156);
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

  public static class XPosValuePartContext extends ParserRuleContext {
    public TerminalNode IDENTIFIER() { return getToken(ParseRuleParser.IDENTIFIER, 0); }

    public XPosValuePartContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() { return RULE_xPosValuePart; }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterXPosValuePart(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitXPosValuePart(this);
    }

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
      if (visitor instanceof ParseRuleVisitor)
        return ((ParseRuleVisitor<? extends T>) visitor).visitXPosValuePart(this);
      else return visitor.visitChildren(this);
    }
  }

  public final XPosValuePartContext xPosValuePart() throws RecognitionException {
    XPosValuePartContext _localctx = new XPosValuePartContext(_ctx, getState());
    enterRule(_localctx, 38, RULE_xPosValuePart);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(157);
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
    enterRule(_localctx, 40, RULE_element);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(159);
        text();
        setState(162);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == HASH) {
          {
            setState(160);
            match(HASH);
            setState(161);
            caseTail();
          }
        }

        setState(166);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__10) {
          {
            setState(164);
            match(T__10);
            setState(165);
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
    enterRule(_localctx, 42, RULE_namedEntityToRole);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(168);
        namedEntity();
        setState(170);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__5) {
          {
            setState(169);
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
    enterRule(_localctx, 44, RULE_toRole);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(172);
        match(T__5);
        setState(173);
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
    enterRule(_localctx, 46, RULE_namedEntity);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(175);
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
    enterRule(_localctx, 48, RULE_role);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(177);
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
    enterRule(_localctx, 50, RULE_text);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(179);
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
    enterRule(_localctx, 52, RULE_id);
    int _la;
    try {
      setState(189);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case STAR:
          enterOuterAlt(_localctx, 1);
        {
          setState(181);
          match(STAR);
        }
        break;
        case LEMMA:
        case IDENTIFIER:
          enterOuterAlt(_localctx, 2);
        {
          setState(186);
          _errHandler.sync(this);
          _la = _input.LA(1);
          if (_la == LEMMA) {
            {
              setState(182);
              match(LEMMA);
              setState(184);
              _errHandler.sync(this);
              switch (getInterpreter().adaptivePredict(_input, 14, _ctx)) {
                case 1: {
                  setState(183);
                  functions();
                }
                break;
              }
            }
          }

          setState(188);
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
    enterRule(_localctx, 54, RULE_lemmas);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(191);
        match(IDENTIFIER);
        setState(196);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__11) {
          {
            {
              setState(192);
              match(T__11);
              setState(193);
              match(IDENTIFIER);
            }
          }
          setState(198);
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
    enterRule(_localctx, 56, RULE_functions);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(202);
        _errHandler.sync(this);
        _alt = 1;
        do {
          switch (_alt) {
            case 1: {
              {
                setState(199);
                functionName();
                setState(200);
                match(T__5);
              }
            }
            break;
            default:
              throw new NoViableAltException(this);
          }
          setState(204);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 18, _ctx);
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
    enterRule(_localctx, 58, RULE_functionName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(206);
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
    enterRule(_localctx, 60, RULE_caseTail);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(208);
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
      "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\23\u00d5\4\2\t\2" +
          "\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
          "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
          "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" +
          "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \3\2" +
          "\5\2B\n\2\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5\5\5M\n\5\3\5\3\5\5\5Q\n" +
          "\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\7\ta\n\t\f" +
          "\t\16\td\13\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\nm\n\n\3\13\5\13p\n\13\3\13" +
          "\3\13\3\f\3\f\5\fv\n\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\7\16\177\n\16\f" +
          "\16\16\16\u0082\13\16\3\17\3\17\3\20\5\20\u0087\n\20\3\20\5\20\u008a\n" +
          "\20\3\20\3\20\3\21\3\21\3\21\3\21\3\22\3\22\3\23\3\23\3\23\3\23\3\24\3" +
          "\24\3\24\7\24\u009b\n\24\f\24\16\24\u009e\13\24\3\25\3\25\3\26\3\26\3" +
          "\26\5\26\u00a5\n\26\3\26\3\26\5\26\u00a9\n\26\3\27\3\27\5\27\u00ad\n\27" +
          "\3\30\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\34\5\34\u00bb" +
          "\n\34\5\34\u00bd\n\34\3\34\5\34\u00c0\n\34\3\35\3\35\3\35\7\35\u00c5\n" +
          "\35\f\35\16\35\u00c8\13\35\3\36\3\36\3\36\6\36\u00cd\n\36\r\36\16\36\u00ce" +
          "\3\37\3\37\3 \3 \3 \2\3\20!\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"" +
          "$&(*,.\60\62\64\668:<>\2\3\4\2\17\17\22\22\2\u00c8\2A\3\2\2\2\4F\3\2\2" +
          "\2\6I\3\2\2\2\bL\3\2\2\2\nR\3\2\2\2\fT\3\2\2\2\16V\3\2\2\2\20X\3\2\2\2" +
          "\22l\3\2\2\2\24o\3\2\2\2\26s\3\2\2\2\30w\3\2\2\2\32{\3\2\2\2\34\u0083" +
          "\3\2\2\2\36\u0086\3\2\2\2 \u008d\3\2\2\2\"\u0091\3\2\2\2$\u0093\3\2\2" +
          "\2&\u0097\3\2\2\2(\u009f\3\2\2\2*\u00a1\3\2\2\2,\u00aa\3\2\2\2.\u00ae" +
          "\3\2\2\2\60\u00b1\3\2\2\2\62\u00b3\3\2\2\2\64\u00b5\3\2\2\2\66\u00bf\3" +
          "\2\2\28\u00c1\3\2\2\2:\u00cc\3\2\2\2<\u00d0\3\2\2\2>\u00d2\3\2\2\2@B\5" +
          "\4\3\2A@\3\2\2\2AB\3\2\2\2BC\3\2\2\2CD\5\b\5\2DE\7\2\2\3E\3\3\2\2\2FG" +
          "\5\6\4\2GH\7\3\2\2H\5\3\2\2\2IJ\5\66\34\2J\7\3\2\2\2KM\5\f\7\2LK\3\2\2" +
          "\2LM\3\2\2\2MN\3\2\2\2NP\5\n\6\2OQ\5\16\b\2PO\3\2\2\2PQ\3\2\2\2Q\t\3\2" +
          "\2\2RS\5\36\20\2S\13\3\2\2\2TU\5\20\t\2U\r\3\2\2\2VW\5\22\n\2W\17\3\2" +
          "\2\2XY\b\t\1\2YZ\5\36\20\2Z[\5\24\13\2[b\3\2\2\2\\]\f\4\2\2]^\5\36\20" +
          "\2^_\5\24\13\2_a\3\2\2\2`\\\3\2\2\2ad\3\2\2\2b`\3\2\2\2bc\3\2\2\2c\21" +
          "\3\2\2\2db\3\2\2\2ef\5\26\f\2fg\5\36\20\2gh\5\22\n\2hm\3\2\2\2ij\5\26" +
          "\f\2jk\5\36\20\2km\3\2\2\2le\3\2\2\2li\3\2\2\2m\23\3\2\2\2np\5\30\r\2" +
          "on\3\2\2\2op\3\2\2\2pq\3\2\2\2qr\7\4\2\2r\25\3\2\2\2su\7\5\2\2tv\5\30" +
          "\r\2ut\3\2\2\2uv\3\2\2\2v\27\3\2\2\2wx\7\6\2\2xy\5\32\16\2yz\7\7\2\2z" +
          "\31\3\2\2\2{\u0080\5\34\17\2|}\7\b\2\2}\177\5\34\17\2~|\3\2\2\2\177\u0082" +
          "\3\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\33\3\2\2\2\u0082\u0080" +
          "\3\2\2\2\u0083\u0084\7\22\2\2\u0084\35\3\2\2\2\u0085\u0087\5 \21\2\u0086" +
          "\u0085\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0089\3\2\2\2\u0088\u008a\5$" +
          "\23\2\u0089\u0088\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008b\3\2\2\2\u008b" +
          "\u008c\5*\26\2\u008c\37\3\2\2\2\u008d\u008e\7\t\2\2\u008e\u008f\5\"\22" +
          "\2\u008f\u0090\7\n\2\2\u0090!\3\2\2\2\u0091\u0092\5\66\34\2\u0092#\3\2" +
          "\2\2\u0093\u0094\7\13\2\2\u0094\u0095\5&\24\2\u0095\u0096\7\f\2\2\u0096" +
          "%\3\2\2\2\u0097\u009c\5(\25\2\u0098\u0099\7\b\2\2\u0099\u009b\5(\25\2" +
          "\u009a\u0098\3\2\2\2\u009b\u009e\3\2\2\2\u009c\u009a\3\2\2\2\u009c\u009d" +
          "\3\2\2\2\u009d\'\3\2\2\2\u009e\u009c\3\2\2\2\u009f\u00a0\7\22\2\2\u00a0" +
          ")\3\2\2\2\u00a1\u00a4\5\64\33\2\u00a2\u00a3\7\21\2\2\u00a3\u00a5\5> \2" +
          "\u00a4\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a8\3\2\2\2\u00a6\u00a7" +
          "\7\r\2\2\u00a7\u00a9\5,\27\2\u00a8\u00a6\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9" +
          "+\3\2\2\2\u00aa\u00ac\5\60\31\2\u00ab\u00ad\5.\30\2\u00ac\u00ab\3\2\2" +
          "\2\u00ac\u00ad\3\2\2\2\u00ad-\3\2\2\2\u00ae\u00af\7\b\2\2\u00af\u00b0" +
          "\5\62\32\2\u00b0/\3\2\2\2\u00b1\u00b2\5\66\34\2\u00b2\61\3\2\2\2\u00b3" +
          "\u00b4\5\66\34\2\u00b4\63\3\2\2\2\u00b5\u00b6\5\66\34\2\u00b6\65\3\2\2" +
          "\2\u00b7\u00c0\7\17\2\2\u00b8\u00ba\7\20\2\2\u00b9\u00bb\5:\36\2\u00ba" +
          "\u00b9\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00bd\3\2\2\2\u00bc\u00b8\3\2" +
          "\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\u00c0\58\35\2\u00bf" +
          "\u00b7\3\2\2\2\u00bf\u00bc\3\2\2\2\u00c0\67\3\2\2\2\u00c1\u00c6\7\22\2" +
          "\2\u00c2\u00c3\7\16\2\2\u00c3\u00c5\7\22\2\2\u00c4\u00c2\3\2\2\2\u00c5" +
          "\u00c8\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c79\3\2\2\2" +
          "\u00c8\u00c6\3\2\2\2\u00c9\u00ca\5<\37\2\u00ca\u00cb\7\b\2\2\u00cb\u00cd" +
          "\3\2\2\2\u00cc\u00c9\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce\u00cc\3\2\2\2\u00ce" +
          "\u00cf\3\2\2\2\u00cf;\3\2\2\2\u00d0\u00d1\7\22\2\2\u00d1=\3\2\2\2\u00d2" +
          "\u00d3\t\2\2\2\u00d3?\3\2\2\2\25ALPblou\u0080\u0086\u0089\u009c\u00a4" +
          "\u00a8\u00ac\u00ba\u00bc\u00bf\u00c6\u00ce";
  public static final ATN _ATN =
      new ATNDeserializer().deserialize(_serializedATN.toCharArray());

  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}