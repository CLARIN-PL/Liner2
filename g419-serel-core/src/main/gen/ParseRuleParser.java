// Generated from /home/user57/NLPWR/projects/Liner2/g419-serel-core/src/main/antlr/ParseRule.g4 by ANTLR 4.8
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ParseRuleParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		STAR=10, LEMMA=11, IDENTIFIER=12, WS=13;
	public static final int
		RULE_start = 0, RULE_semRel = 1, RULE_semRelName = 2, RULE_expression = 3, 
		RULE_rootNode = 4, RULE_rootLeftExpression = 5, RULE_rootRightExpression = 6, 
		RULE_leftExpression = 7, RULE_rightExpression = 8, RULE_leftEdge = 9, 
		RULE_rightEdge = 10, RULE_depRel = 11, RULE_depRelValue = 12, RULE_node = 13, 
		RULE_xPos = 14, RULE_xPosValue = 15, RULE_element = 16, RULE_namedEntityToRole = 17, 
		RULE_toRole = 18, RULE_namedEntity = 19, RULE_role = 20, RULE_text = 21, 
		RULE_id = 22;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "semRel", "semRelName", "expression", "rootNode", "rootLeftExpression", 
			"rootRightExpression", "leftExpression", "rightExpression", "leftEdge", 
			"rightEdge", "depRel", "depRelValue", "node", "xPos", "xPosValue", "element", 
			"namedEntityToRole", "toRole", "namedEntity", "role", "text", "id"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'::'", "'>'", "'<'", "'('", "')'", "'['", "']'", "'/'", "':'", 
			"'*'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
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
	public String getGrammarFileName() { return "ParseRule.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ParseRuleParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class StartContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ParseRuleParser.EOF, 0); }
		public SemRelContext semRel() {
			return getRuleContext(SemRelContext.class,0);
		}
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(46);
				semRel();
				}
				break;
			}
			setState(49);
			expression();
			setState(50);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SemRelContext extends ParserRuleContext {
		public SemRelNameContext semRelName() {
			return getRuleContext(SemRelNameContext.class,0);
		}
		public SemRelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_semRel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterSemRel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitSemRel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitSemRel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SemRelContext semRel() throws RecognitionException {
		SemRelContext _localctx = new SemRelContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_semRel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			semRelName();
			setState(53);
			match(T__0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SemRelNameContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public SemRelNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_semRelName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterSemRelName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitSemRelName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitSemRelName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SemRelNameContext semRelName() throws RecognitionException {
		SemRelNameContext _localctx = new SemRelNameContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_semRelName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(55);
			id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public RootNodeContext rootNode() {
			return getRuleContext(RootNodeContext.class,0);
		}
		public RootLeftExpressionContext rootLeftExpression() {
			return getRuleContext(RootLeftExpressionContext.class,0);
		}
		public RootRightExpressionContext rootRightExpression() {
			return getRuleContext(RootRightExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitExpression(this);
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
			setState(58);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(57);
				rootLeftExpression();
				}
				break;
			}
			setState(60);
			rootNode();
			setState(62);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(61);
				rootRightExpression();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RootNodeContext extends ParserRuleContext {
		public NodeContext node() {
			return getRuleContext(NodeContext.class,0);
		}
		public RootNodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rootNode; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterRootNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitRootNode(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitRootNode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootNodeContext rootNode() throws RecognitionException {
		RootNodeContext _localctx = new RootNodeContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_rootNode);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			node();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RootLeftExpressionContext extends ParserRuleContext {
		public LeftExpressionContext leftExpression() {
			return getRuleContext(LeftExpressionContext.class,0);
		}
		public RootLeftExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rootLeftExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterRootLeftExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitRootLeftExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitRootLeftExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootLeftExpressionContext rootLeftExpression() throws RecognitionException {
		RootLeftExpressionContext _localctx = new RootLeftExpressionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_rootLeftExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			leftExpression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RootRightExpressionContext extends ParserRuleContext {
		public RightExpressionContext rightExpression() {
			return getRuleContext(RightExpressionContext.class,0);
		}
		public RootRightExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rootRightExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterRootRightExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitRootRightExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitRootRightExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootRightExpressionContext rootRightExpression() throws RecognitionException {
		RootRightExpressionContext _localctx = new RootRightExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_rootRightExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68);
			rightExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LeftExpressionContext extends ParserRuleContext {
		public NodeContext node() {
			return getRuleContext(NodeContext.class,0);
		}
		public LeftEdgeContext leftEdge() {
			return getRuleContext(LeftEdgeContext.class,0);
		}
		public LeftExpressionContext leftExpression() {
			return getRuleContext(LeftExpressionContext.class,0);
		}
		public LeftExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_leftExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterLeftExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitLeftExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitLeftExpression(this);
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
			setState(71);
			node();
			setState(72);
			leftEdge();
			}
			_ctx.stop = _input.LT(-1);
			setState(80);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new LeftExpressionContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_leftExpression);
					setState(74);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(75);
					node();
					setState(76);
					leftEdge();
					}
					} 
				}
				setState(82);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class RightExpressionContext extends ParserRuleContext {
		public RightEdgeContext rightEdge() {
			return getRuleContext(RightEdgeContext.class,0);
		}
		public NodeContext node() {
			return getRuleContext(NodeContext.class,0);
		}
		public RightExpressionContext rightExpression() {
			return getRuleContext(RightExpressionContext.class,0);
		}
		public RightExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rightExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterRightExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitRightExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitRightExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RightExpressionContext rightExpression() throws RecognitionException {
		RightExpressionContext _localctx = new RightExpressionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_rightExpression);
		try {
			setState(90);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(83);
				rightEdge();
				setState(84);
				node();
				setState(85);
				rightExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(87);
				rightEdge();
				setState(88);
				node();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LeftEdgeContext extends ParserRuleContext {
		public DepRelContext depRel() {
			return getRuleContext(DepRelContext.class,0);
		}
		public LeftEdgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_leftEdge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterLeftEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitLeftEdge(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitLeftEdge(this);
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
			setState(93);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(92);
				depRel();
				}
			}

			setState(95);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RightEdgeContext extends ParserRuleContext {
		public DepRelContext depRel() {
			return getRuleContext(DepRelContext.class,0);
		}
		public RightEdgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rightEdge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterRightEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitRightEdge(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitRightEdge(this);
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
			setState(97);
			match(T__2);
			setState(99);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(98);
				depRel();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DepRelContext extends ParserRuleContext {
		public DepRelValueContext depRelValue() {
			return getRuleContext(DepRelValueContext.class,0);
		}
		public DepRelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_depRel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterDepRel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitDepRel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitDepRel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DepRelContext depRel() throws RecognitionException {
		DepRelContext _localctx = new DepRelContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_depRel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			match(T__3);
			setState(102);
			depRelValue();
			setState(103);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DepRelValueContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public DepRelValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_depRelValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterDepRelValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitDepRelValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitDepRelValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DepRelValueContext depRelValue() throws RecognitionException {
		DepRelValueContext _localctx = new DepRelValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_depRelValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeContext extends ParserRuleContext {
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public XPosContext xPos() {
			return getRuleContext(XPosContext.class,0);
		}
		public NodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitNode(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitNode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NodeContext node() throws RecognitionException {
		NodeContext _localctx = new NodeContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_node);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__5) {
				{
				setState(107);
				xPos();
				}
			}

			setState(110);
			element();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class XPosContext extends ParserRuleContext {
		public XPosValueContext xPosValue() {
			return getRuleContext(XPosValueContext.class,0);
		}
		public XPosContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xPos; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterXPos(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitXPos(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitXPos(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XPosContext xPos() throws RecognitionException {
		XPosContext _localctx = new XPosContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_xPos);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			match(T__5);
			setState(113);
			xPosValue();
			setState(114);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class XPosValueContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public XPosValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xPosValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterXPosValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitXPosValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitXPosValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XPosValueContext xPosValue() throws RecognitionException {
		XPosValueContext _localctx = new XPosValueContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_xPosValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementContext extends ParserRuleContext {
		public TextContext text() {
			return getRuleContext(TextContext.class,0);
		}
		public NamedEntityToRoleContext namedEntityToRole() {
			return getRuleContext(NamedEntityToRoleContext.class,0);
		}
		public ElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_element);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118);
			text();
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__7) {
				{
				setState(119);
				match(T__7);
				setState(120);
				namedEntityToRole();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamedEntityToRoleContext extends ParserRuleContext {
		public NamedEntityContext namedEntity() {
			return getRuleContext(NamedEntityContext.class,0);
		}
		public ToRoleContext toRole() {
			return getRuleContext(ToRoleContext.class,0);
		}
		public NamedEntityToRoleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedEntityToRole; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterNamedEntityToRole(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitNamedEntityToRole(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitNamedEntityToRole(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedEntityToRoleContext namedEntityToRole() throws RecognitionException {
		NamedEntityToRoleContext _localctx = new NamedEntityToRoleContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_namedEntityToRole);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			namedEntity();
			setState(125);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__8) {
				{
				setState(124);
				toRole();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ToRoleContext extends ParserRuleContext {
		public RoleContext role() {
			return getRuleContext(RoleContext.class,0);
		}
		public ToRoleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_toRole; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterToRole(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitToRole(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitToRole(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ToRoleContext toRole() throws RecognitionException {
		ToRoleContext _localctx = new ToRoleContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_toRole);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(T__8);
			setState(128);
			role();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamedEntityContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public NamedEntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedEntity; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterNamedEntity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitNamedEntity(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitNamedEntity(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedEntityContext namedEntity() throws RecognitionException {
		NamedEntityContext _localctx = new NamedEntityContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_namedEntity);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RoleContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public RoleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_role; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterRole(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitRole(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitRole(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RoleContext role() throws RecognitionException {
		RoleContext _localctx = new RoleContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_role);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TextContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_text; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterText(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitText(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitText(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TextContext text() throws RecognitionException {
		TextContext _localctx = new TextContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_text);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(ParseRuleParser.STAR, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ParseRuleParser.IDENTIFIER, 0); }
		public TerminalNode LEMMA() { return getToken(ParseRuleParser.LEMMA, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ParseRuleListener ) ((ParseRuleListener)listener).exitId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ParseRuleVisitor ) return ((ParseRuleVisitor<? extends T>)visitor).visitId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_id);
		int _la;
		try {
			setState(141);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(136);
				match(STAR);
				}
				break;
			case LEMMA:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(138);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEMMA) {
					{
					setState(137);
					match(LEMMA);
					}
				}

				setState(140);
				match(IDENTIFIER);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 7:
			return leftExpression_sempred((LeftExpressionContext)_localctx, predIndex);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\17\u0092\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\3\2\5\2\62"+
		"\n\2\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5\5\5=\n\5\3\5\3\5\5\5A\n\5\3\6"+
		"\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\7\tQ\n\t\f\t\16\t"+
		"T\13\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n]\n\n\3\13\5\13`\n\13\3\13\3\13"+
		"\3\f\3\f\5\ff\n\f\3\r\3\r\3\r\3\r\3\16\3\16\3\17\5\17o\n\17\3\17\3\17"+
		"\3\20\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\22\5\22|\n\22\3\23\3\23\5\23"+
		"\u0080\n\23\3\24\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\5\30"+
		"\u008d\n\30\3\30\5\30\u0090\n\30\3\30\2\3\20\31\2\4\6\b\n\f\16\20\22\24"+
		"\26\30\32\34\36 \"$&(*,.\2\2\2\u0086\2\61\3\2\2\2\4\66\3\2\2\2\69\3\2"+
		"\2\2\b<\3\2\2\2\nB\3\2\2\2\fD\3\2\2\2\16F\3\2\2\2\20H\3\2\2\2\22\\\3\2"+
		"\2\2\24_\3\2\2\2\26c\3\2\2\2\30g\3\2\2\2\32k\3\2\2\2\34n\3\2\2\2\36r\3"+
		"\2\2\2 v\3\2\2\2\"x\3\2\2\2$}\3\2\2\2&\u0081\3\2\2\2(\u0084\3\2\2\2*\u0086"+
		"\3\2\2\2,\u0088\3\2\2\2.\u008f\3\2\2\2\60\62\5\4\3\2\61\60\3\2\2\2\61"+
		"\62\3\2\2\2\62\63\3\2\2\2\63\64\5\b\5\2\64\65\7\2\2\3\65\3\3\2\2\2\66"+
		"\67\5\6\4\2\678\7\3\2\28\5\3\2\2\29:\5.\30\2:\7\3\2\2\2;=\5\f\7\2<;\3"+
		"\2\2\2<=\3\2\2\2=>\3\2\2\2>@\5\n\6\2?A\5\16\b\2@?\3\2\2\2@A\3\2\2\2A\t"+
		"\3\2\2\2BC\5\34\17\2C\13\3\2\2\2DE\5\20\t\2E\r\3\2\2\2FG\5\22\n\2G\17"+
		"\3\2\2\2HI\b\t\1\2IJ\5\34\17\2JK\5\24\13\2KR\3\2\2\2LM\f\4\2\2MN\5\34"+
		"\17\2NO\5\24\13\2OQ\3\2\2\2PL\3\2\2\2QT\3\2\2\2RP\3\2\2\2RS\3\2\2\2S\21"+
		"\3\2\2\2TR\3\2\2\2UV\5\26\f\2VW\5\34\17\2WX\5\22\n\2X]\3\2\2\2YZ\5\26"+
		"\f\2Z[\5\34\17\2[]\3\2\2\2\\U\3\2\2\2\\Y\3\2\2\2]\23\3\2\2\2^`\5\30\r"+
		"\2_^\3\2\2\2_`\3\2\2\2`a\3\2\2\2ab\7\4\2\2b\25\3\2\2\2ce\7\5\2\2df\5\30"+
		"\r\2ed\3\2\2\2ef\3\2\2\2f\27\3\2\2\2gh\7\6\2\2hi\5\32\16\2ij\7\7\2\2j"+
		"\31\3\2\2\2kl\5.\30\2l\33\3\2\2\2mo\5\36\20\2nm\3\2\2\2no\3\2\2\2op\3"+
		"\2\2\2pq\5\"\22\2q\35\3\2\2\2rs\7\b\2\2st\5 \21\2tu\7\t\2\2u\37\3\2\2"+
		"\2vw\5.\30\2w!\3\2\2\2x{\5,\27\2yz\7\n\2\2z|\5$\23\2{y\3\2\2\2{|\3\2\2"+
		"\2|#\3\2\2\2}\177\5(\25\2~\u0080\5&\24\2\177~\3\2\2\2\177\u0080\3\2\2"+
		"\2\u0080%\3\2\2\2\u0081\u0082\7\13\2\2\u0082\u0083\5*\26\2\u0083\'\3\2"+
		"\2\2\u0084\u0085\5.\30\2\u0085)\3\2\2\2\u0086\u0087\5.\30\2\u0087+\3\2"+
		"\2\2\u0088\u0089\5.\30\2\u0089-\3\2\2\2\u008a\u0090\7\f\2\2\u008b\u008d"+
		"\7\r\2\2\u008c\u008b\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008e\3\2\2\2\u008e"+
		"\u0090\7\16\2\2\u008f\u008a\3\2\2\2\u008f\u008c\3\2\2\2\u0090/\3\2\2\2"+
		"\16\61<@R\\_en{\177\u008c\u008f";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}