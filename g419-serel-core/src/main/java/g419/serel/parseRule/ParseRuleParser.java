// Generated from /home/user57/NLPWR/projects/CmdANTLR/src/ParseRule.g4 by ANTLR 4.8
package g419.serel.parseRule;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ParseRuleParser extends Parser {
    static {
        RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            STAR = 10, LEMMA = 11, IDENTIFIER = 12, WS = 13;
    public static final int
            RULE_start = 0, RULE_semRel = 1, RULE_semRelName = 2, RULE_expression = 3,
            RULE_rootNode = 4, RULE_leftExpression = 5, RULE_rightExpression = 6,
            RULE_leftEdge = 7, RULE_rightEdge = 8, RULE_depRel = 9, RULE_depRelValue = 10,
            RULE_node = 11, RULE_xPos = 12, RULE_xPosValue = 13, RULE_element = 14,
            RULE_namedEntityToRole = 15, RULE_namedEntity = 16, RULE_role = 17, RULE_text = 18,
            RULE_id = 19;

    private static String[] makeRuleNames() {
        return new String[]{
                "start", "semRel", "semRelName", "expression", "rootNode", "leftExpression",
                "rightExpression", "leftEdge", "rightEdge", "depRel", "depRelValue",
                "node", "xPos", "xPosValue", "element", "namedEntityToRole", "namedEntity",
                "role", "text", "id"
        };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'::'", "'>'", "'<'", "'('", "')'", "'['", "']'", "'/'", "':'",
                "'*'", "'^'"
        };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, null, null, null, null, "STAR", "LEMMA",
                "IDENTIFIER", "WS"
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
    public String getGrammarFileName() {
        return "ParseRule.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public ParseRuleParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    public static class StartContext extends ParserRuleContext {
        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public TerminalNode EOF() {
            return getToken(ParseRuleParser.EOF, 0);
        }

        public SemRelContext semRel() {
            return getRuleContext(SemRelContext.class, 0);
        }

        public StartContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_start;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterStart(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitStart(this);
        }
    }

    public final StartContext start() throws RecognitionException {
        StartContext _localctx = new StartContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_start);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(41);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
                    case 1: {
                        setState(40);
                        semRel();
                    }
                    break;
                }
                setState(43);
                expression();
                setState(44);
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
        public int getRuleIndex() {
            return RULE_semRel;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterSemRel(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitSemRel(this);
        }
    }

    public final SemRelContext semRel() throws RecognitionException {
        SemRelContext _localctx = new SemRelContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_semRel);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(46);
                semRelName();
                setState(47);
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
        public int getRuleIndex() {
            return RULE_semRelName;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterSemRelName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitSemRelName(this);
        }
    }

    public final SemRelNameContext semRelName() throws RecognitionException {
        SemRelNameContext _localctx = new SemRelNameContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_semRelName);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(49);
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

        public LeftExpressionContext leftExpression() {
            return getRuleContext(LeftExpressionContext.class, 0);
        }

        public RightExpressionContext rightExpression() {
            return getRuleContext(RightExpressionContext.class, 0);
        }

        public ExpressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_expression;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterExpression(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitExpression(this);
        }
    }

    public final ExpressionContext expression() throws RecognitionException {
        ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_expression);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(52);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 1, _ctx)) {
                    case 1: {
                        setState(51);
                        leftExpression(0);
                    }
                    break;
                }
                setState(54);
                rootNode();
                setState(56);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__2) {
                    {
                        setState(55);
                        rightExpression();
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
        public int getRuleIndex() {
            return RULE_rootNode;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRootNode(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRootNode(this);
        }
    }

    public final RootNodeContext rootNode() throws RecognitionException {
        RootNodeContext _localctx = new RootNodeContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_rootNode);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(58);
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
        public int getRuleIndex() {
            return RULE_leftExpression;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterLeftExpression(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitLeftExpression(this);
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
        int _startState = 10;
        enterRecursionRule(_localctx, 10, RULE_leftExpression, _p);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(61);
                    node();
                    setState(62);
                    leftEdge();
                }
                _ctx.stop = _input.LT(-1);
                setState(70);
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
                                setState(64);
                                if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                                setState(65);
                                node();
                                setState(66);
                                leftEdge();
                            }
                        }
                    }
                    setState(72);
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
        public int getRuleIndex() {
            return RULE_rightExpression;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRightExpression(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRightExpression(this);
        }
    }

    public final RightExpressionContext rightExpression() throws RecognitionException {
        RightExpressionContext _localctx = new RightExpressionContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_rightExpression);
        try {
            setState(80);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 4, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(73);
                    rightEdge();
                    setState(74);
                    node();
                    setState(75);
                    rightExpression();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(77);
                    rightEdge();
                    setState(78);
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
        public int getRuleIndex() {
            return RULE_leftEdge;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterLeftEdge(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitLeftEdge(this);
        }
    }

    public final LeftEdgeContext leftEdge() throws RecognitionException {
        LeftEdgeContext _localctx = new LeftEdgeContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_leftEdge);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(83);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__3) {
                    {
                        setState(82);
                        depRel();
                    }
                }

                setState(85);
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
        public int getRuleIndex() {
            return RULE_rightEdge;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRightEdge(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRightEdge(this);
        }
    }

    public final RightEdgeContext rightEdge() throws RecognitionException {
        RightEdgeContext _localctx = new RightEdgeContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_rightEdge);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(87);
                match(T__2);
                setState(89);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__3) {
                    {
                        setState(88);
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
        public int getRuleIndex() {
            return RULE_depRel;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterDepRel(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitDepRel(this);
        }
    }

    public final DepRelContext depRel() throws RecognitionException {
        DepRelContext _localctx = new DepRelContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_depRel);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(91);
                match(T__3);
                setState(92);
                depRelValue();
                setState(93);
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
        public IdContext id() {
            return getRuleContext(IdContext.class, 0);
        }

        public DepRelValueContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_depRelValue;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterDepRelValue(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitDepRelValue(this);
        }
    }

    public final DepRelValueContext depRelValue() throws RecognitionException {
        DepRelValueContext _localctx = new DepRelValueContext(_ctx, getState());
        enterRule(_localctx, 20, RULE_depRelValue);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(95);
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
        public int getRuleIndex() {
            return RULE_node;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterNode(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitNode(this);
        }
    }

    public final NodeContext node() throws RecognitionException {
        NodeContext _localctx = new NodeContext(_ctx, getState());
        enterRule(_localctx, 22, RULE_node);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(98);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__5) {
                    {
                        setState(97);
                        xPos();
                    }
                }

                setState(100);
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
        public int getRuleIndex() {
            return RULE_xPos;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterXPos(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitXPos(this);
        }
    }

    public final XPosContext xPos() throws RecognitionException {
        XPosContext _localctx = new XPosContext(_ctx, getState());
        enterRule(_localctx, 24, RULE_xPos);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(102);
                match(T__5);
                setState(103);
                xPosValue();
                setState(104);
                match(T__6);
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
        public int getRuleIndex() {
            return RULE_xPosValue;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterXPosValue(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitXPosValue(this);
        }
    }

    public final XPosValueContext xPosValue() throws RecognitionException {
        XPosValueContext _localctx = new XPosValueContext(_ctx, getState());
        enterRule(_localctx, 26, RULE_xPosValue);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(106);
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

        public NamedEntityToRoleContext namedEntityToRole() {
            return getRuleContext(NamedEntityToRoleContext.class, 0);
        }

        public ElementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_element;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterElement(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitElement(this);
        }
    }

    public final ElementContext element() throws RecognitionException {
        ElementContext _localctx = new ElementContext(_ctx, getState());
        enterRule(_localctx, 28, RULE_element);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(108);
                text();
                setState(111);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__7) {
                    {
                        setState(109);
                        match(T__7);
                        setState(110);
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

        public RoleContext role() {
            return getRuleContext(RoleContext.class, 0);
        }

        public NamedEntityToRoleContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_namedEntityToRole;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterNamedEntityToRole(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitNamedEntityToRole(this);
        }
    }

    public final NamedEntityToRoleContext namedEntityToRole() throws RecognitionException {
        NamedEntityToRoleContext _localctx = new NamedEntityToRoleContext(_ctx, getState());
        enterRule(_localctx, 30, RULE_namedEntityToRole);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(113);
                namedEntity();
                setState(114);
                match(T__8);
                setState(115);
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
        public int getRuleIndex() {
            return RULE_namedEntity;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterNamedEntity(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitNamedEntity(this);
        }
    }

    public final NamedEntityContext namedEntity() throws RecognitionException {
        NamedEntityContext _localctx = new NamedEntityContext(_ctx, getState());
        enterRule(_localctx, 32, RULE_namedEntity);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(117);
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
        public int getRuleIndex() {
            return RULE_role;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterRole(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitRole(this);
        }
    }

    public final RoleContext role() throws RecognitionException {
        RoleContext _localctx = new RoleContext(_ctx, getState());
        enterRule(_localctx, 34, RULE_role);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(119);
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
        public int getRuleIndex() {
            return RULE_text;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterText(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitText(this);
        }
    }

    public final TextContext text() throws RecognitionException {
        TextContext _localctx = new TextContext(_ctx, getState());
        enterRule(_localctx, 36, RULE_text);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(121);
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
        public TerminalNode STAR() {
            return getToken(ParseRuleParser.STAR, 0);
        }

        public TerminalNode IDENTIFIER() {
            return getToken(ParseRuleParser.IDENTIFIER, 0);
        }

        public TerminalNode LEMMA() {
            return getToken(ParseRuleParser.LEMMA, 0);
        }

        public IdContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_id;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).enterId(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ParseRuleListener) ((ParseRuleListener) listener).exitId(this);
        }
    }

    public final IdContext id() throws RecognitionException {
        IdContext _localctx = new IdContext(_ctx, getState());
        enterRule(_localctx, 38, RULE_id);
        int _la;
        try {
            setState(128);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case STAR:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(123);
                    match(STAR);
                }
                break;
                case LEMMA:
                case IDENTIFIER:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(125);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == LEMMA) {
                        {
                            setState(124);
                            match(LEMMA);
                        }
                    }

                    setState(127);
                    match(IDENTIFIER);
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

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch (ruleIndex) {
            case 5:
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
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\17\u0085\4\2\t\2" +
                    "\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
                    "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
                    "\4\23\t\23\4\24\t\24\4\25\t\25\3\2\5\2,\n\2\3\2\3\2\3\2\3\3\3\3\3\3\3" +
                    "\4\3\4\3\5\5\5\67\n\5\3\5\3\5\5\5;\n\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7" +
                    "\3\7\3\7\7\7G\n\7\f\7\16\7J\13\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\bS\n\b" +
                    "\3\t\5\tV\n\t\3\t\3\t\3\n\3\n\5\n\\\n\n\3\13\3\13\3\13\3\13\3\f\3\f\3" +
                    "\r\5\re\n\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\20\5\20" +
                    "r\n\20\3\21\3\21\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\5\25" +
                    "\u0080\n\25\3\25\5\25\u0083\n\25\3\25\2\3\f\26\2\4\6\b\n\f\16\20\22\24" +
                    "\26\30\32\34\36 \"$&(\2\2\2{\2+\3\2\2\2\4\60\3\2\2\2\6\63\3\2\2\2\b\66" +
                    "\3\2\2\2\n<\3\2\2\2\f>\3\2\2\2\16R\3\2\2\2\20U\3\2\2\2\22Y\3\2\2\2\24" +
                    "]\3\2\2\2\26a\3\2\2\2\30d\3\2\2\2\32h\3\2\2\2\34l\3\2\2\2\36n\3\2\2\2" +
                    " s\3\2\2\2\"w\3\2\2\2$y\3\2\2\2&{\3\2\2\2(\u0082\3\2\2\2*,\5\4\3\2+*\3" +
                    "\2\2\2+,\3\2\2\2,-\3\2\2\2-.\5\b\5\2./\7\2\2\3/\3\3\2\2\2\60\61\5\6\4" +
                    "\2\61\62\7\3\2\2\62\5\3\2\2\2\63\64\5(\25\2\64\7\3\2\2\2\65\67\5\f\7\2" +
                    "\66\65\3\2\2\2\66\67\3\2\2\2\678\3\2\2\28:\5\n\6\29;\5\16\b\2:9\3\2\2" +
                    "\2:;\3\2\2\2;\t\3\2\2\2<=\5\30\r\2=\13\3\2\2\2>?\b\7\1\2?@\5\30\r\2@A" +
                    "\5\20\t\2AH\3\2\2\2BC\f\4\2\2CD\5\30\r\2DE\5\20\t\2EG\3\2\2\2FB\3\2\2" +
                    "\2GJ\3\2\2\2HF\3\2\2\2HI\3\2\2\2I\r\3\2\2\2JH\3\2\2\2KL\5\22\n\2LM\5\30" +
                    "\r\2MN\5\16\b\2NS\3\2\2\2OP\5\22\n\2PQ\5\30\r\2QS\3\2\2\2RK\3\2\2\2RO" +
                    "\3\2\2\2S\17\3\2\2\2TV\5\24\13\2UT\3\2\2\2UV\3\2\2\2VW\3\2\2\2WX\7\4\2" +
                    "\2X\21\3\2\2\2Y[\7\5\2\2Z\\\5\24\13\2[Z\3\2\2\2[\\\3\2\2\2\\\23\3\2\2" +
                    "\2]^\7\6\2\2^_\5\26\f\2_`\7\7\2\2`\25\3\2\2\2ab\5(\25\2b\27\3\2\2\2ce" +
                    "\5\32\16\2dc\3\2\2\2de\3\2\2\2ef\3\2\2\2fg\5\36\20\2g\31\3\2\2\2hi\7\b" +
                    "\2\2ij\5\34\17\2jk\7\t\2\2k\33\3\2\2\2lm\5(\25\2m\35\3\2\2\2nq\5&\24\2" +
                    "op\7\n\2\2pr\5 \21\2qo\3\2\2\2qr\3\2\2\2r\37\3\2\2\2st\5\"\22\2tu\7\13" +
                    "\2\2uv\5$\23\2v!\3\2\2\2wx\5(\25\2x#\3\2\2\2yz\5(\25\2z%\3\2\2\2{|\5(" +
                    "\25\2|\'\3\2\2\2}\u0083\7\f\2\2~\u0080\7\r\2\2\177~\3\2\2\2\177\u0080" +
                    "\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0083\7\16\2\2\u0082}\3\2\2\2\u0082" +
                    "\177\3\2\2\2\u0083)\3\2\2\2\r+\66:HRU[dq\177\u0082";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}