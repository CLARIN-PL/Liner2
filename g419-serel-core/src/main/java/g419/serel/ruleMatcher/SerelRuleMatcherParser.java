// Generated from /home/user57/NLPWR/projects/Liner2/g419-serel-core/src/main/antlr/SerelRuleMatcher.g4 by ANTLR 4.8
package g419.serel.ruleMatcher;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SerelRuleMatcherParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		STAR=10, LEMMA=11, IDENTIFIER=12, WS=13;
	public static final int
		RULE_start = 0, RULE_expression = 1, RULE_semRelName = 2, RULE_depRelValue = 3, 
		RULE_depRel = 4, RULE_leftEdge = 5, RULE_rightEdge = 6, RULE_namedEntity = 7, 
		RULE_role = 8, RULE_text = 9, RULE_namedEntityToRole = 10, RULE_element = 11, 
		RULE_xPosValue = 12, RULE_xPos = 13, RULE_token = 14, RULE_leftExpression = 15, 
		RULE_rightExpression = 16, RULE_id = 17;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "expression", "semRelName", "depRelValue", "depRel", "leftEdge", 
			"rightEdge", "namedEntity", "role", "text", "namedEntityToRole", "element", 
			"xPosValue", "xPos", "token", "leftExpression", "rightExpression", "id"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'::'", "'('", "')'", "'>'", "'<'", "':'", "'/'", "'['", "']'", 
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
	public String getGrammarFileName() { return "SerelRuleMatcher.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SerelRuleMatcherParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class StartContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SerelRuleMatcherParser.EOF, 0); }
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			expression();
			setState(37);
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

	public static class ExpressionContext extends ParserRuleContext {
		public TokenContext token() {
			return getRuleContext(TokenContext.class,0);
		}
		public SemRelNameContext semRelName() {
			return getRuleContext(SemRelNameContext.class,0);
		}
		public LeftExpressionContext leftExpression() {
			return getRuleContext(LeftExpressionContext.class,0);
		}
		public RightExpressionContext rightExpression() {
			return getRuleContext(RightExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(39);
				semRelName();
				}
				break;
			}
			setState(43);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(42);
				leftExpression(0);
				}
				break;
			}
			setState(45);
			token();
			setState(47);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__4) {
				{
				setState(46);
				rightExpression();
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterSemRelName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitSemRelName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitSemRelName(this);
			else return visitor.visitChildren(this);
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
			setState(50);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterDepRelValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitDepRelValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitDepRelValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DepRelValueContext depRelValue() throws RecognitionException {
		DepRelValueContext _localctx = new DepRelValueContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_depRelValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterDepRel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitDepRel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitDepRel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DepRelContext depRel() throws RecognitionException {
		DepRelContext _localctx = new DepRelContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_depRel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			match(T__1);
			setState(55);
			depRelValue();
			setState(56);
			match(T__2);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterLeftEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitLeftEdge(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitLeftEdge(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LeftEdgeContext leftEdge() throws RecognitionException {
		LeftEdgeContext _localctx = new LeftEdgeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_leftEdge);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(58);
				depRel();
				}
			}

			setState(61);
			match(T__3);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterRightEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitRightEdge(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitRightEdge(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RightEdgeContext rightEdge() throws RecognitionException {
		RightEdgeContext _localctx = new RightEdgeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_rightEdge);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			match(T__4);
			setState(65);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(64);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterNamedEntity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitNamedEntity(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitNamedEntity(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedEntityContext namedEntity() throws RecognitionException {
		NamedEntityContext _localctx = new NamedEntityContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_namedEntity);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterRole(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitRole(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitRole(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RoleContext role() throws RecognitionException {
		RoleContext _localctx = new RoleContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_role);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterText(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitText(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitText(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TextContext text() throws RecognitionException {
		TextContext _localctx = new TextContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_text);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
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

	public static class NamedEntityToRoleContext extends ParserRuleContext {
		public NamedEntityContext namedEntity() {
			return getRuleContext(NamedEntityContext.class,0);
		}
		public RoleContext role() {
			return getRuleContext(RoleContext.class,0);
		}
		public NamedEntityToRoleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedEntityToRole; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterNamedEntityToRole(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitNamedEntityToRole(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitNamedEntityToRole(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedEntityToRoleContext namedEntityToRole() throws RecognitionException {
		NamedEntityToRoleContext _localctx = new NamedEntityToRoleContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_namedEntityToRole);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73);
			namedEntity();
			setState(74);
			match(T__5);
			setState(75);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitElement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_element);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			text();
			setState(80);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__6) {
				{
				setState(78);
				match(T__6);
				setState(79);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterXPosValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitXPosValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitXPosValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XPosValueContext xPosValue() throws RecognitionException {
		XPosValueContext _localctx = new XPosValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_xPosValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterXPos(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitXPos(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitXPos(this);
			else return visitor.visitChildren(this);
		}
	}

	public final XPosContext xPos() throws RecognitionException {
		XPosContext _localctx = new XPosContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_xPos);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			match(T__7);
			setState(85);
			xPosValue();
			setState(86);
			match(T__8);
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

	public static class TokenContext extends ParserRuleContext {
		public ElementContext element() {
			return getRuleContext(ElementContext.class,0);
		}
		public XPosContext xPos() {
			return getRuleContext(XPosContext.class,0);
		}
		public TokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_token; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitToken(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitToken(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TokenContext token() throws RecognitionException {
		TokenContext _localctx = new TokenContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_token);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__7) {
				{
				setState(88);
				xPos();
				}
			}

			setState(91);
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

	public static class LeftExpressionContext extends ParserRuleContext {
		public TokenContext token() {
			return getRuleContext(TokenContext.class,0);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterLeftExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitLeftExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitLeftExpression(this);
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
		int _startState = 30;
		enterRecursionRule(_localctx, 30, RULE_leftExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(94);
			token();
			setState(95);
			leftEdge();
			}
			_ctx.stop = _input.LT(-1);
			setState(103);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new LeftExpressionContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_leftExpression);
					setState(97);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(98);
					token();
					setState(99);
					leftEdge();
					}
					} 
				}
				setState(105);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
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
		public TokenContext token() {
			return getRuleContext(TokenContext.class,0);
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
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterRightExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitRightExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitRightExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RightExpressionContext rightExpression() throws RecognitionException {
		RightExpressionContext _localctx = new RightExpressionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_rightExpression);
		try {
			setState(113);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(106);
				rightEdge();
				setState(107);
				token();
				setState(108);
				rightExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(110);
				rightEdge();
				setState(111);
				token();
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

	public static class IdContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(SerelRuleMatcherParser.STAR, 0); }
		public TerminalNode IDENTIFIER() { return getToken(SerelRuleMatcherParser.IDENTIFIER, 0); }
		public TerminalNode LEMMA() { return getToken(SerelRuleMatcherParser.LEMMA, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerelRuleMatcherListener ) ((SerelRuleMatcherListener)listener).exitId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerelRuleMatcherVisitor ) return ((SerelRuleMatcherVisitor<? extends T>)visitor).visitId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_id);
		int _la;
		try {
			setState(120);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(115);
				match(STAR);
				}
				break;
			case LEMMA:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEMMA) {
					{
					setState(116);
					match(LEMMA);
					}
				}

				setState(119);
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
		case 15:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\17}\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22\4\23"+
		"\t\23\3\2\3\2\3\2\3\3\5\3+\n\3\3\3\5\3.\n\3\3\3\3\3\5\3\62\n\3\3\4\3\4"+
		"\3\4\3\5\3\5\3\6\3\6\3\6\3\6\3\7\5\7>\n\7\3\7\3\7\3\b\3\b\5\bD\n\b\3\t"+
		"\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\r\3\r\3\r\5\rS\n\r\3\16\3\16"+
		"\3\17\3\17\3\17\3\17\3\20\5\20\\\n\20\3\20\3\20\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\7\21h\n\21\f\21\16\21k\13\21\3\22\3\22\3\22\3\22\3"+
		"\22\3\22\3\22\5\22t\n\22\3\23\3\23\5\23x\n\23\3\23\5\23{\n\23\3\23\2\3"+
		" \24\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$\2\2\2u\2&\3\2\2\2\4*\3"+
		"\2\2\2\6\63\3\2\2\2\b\66\3\2\2\2\n8\3\2\2\2\f=\3\2\2\2\16A\3\2\2\2\20"+
		"E\3\2\2\2\22G\3\2\2\2\24I\3\2\2\2\26K\3\2\2\2\30O\3\2\2\2\32T\3\2\2\2"+
		"\34V\3\2\2\2\36[\3\2\2\2 _\3\2\2\2\"s\3\2\2\2$z\3\2\2\2&\'\5\4\3\2\'("+
		"\7\2\2\3(\3\3\2\2\2)+\5\6\4\2*)\3\2\2\2*+\3\2\2\2+-\3\2\2\2,.\5 \21\2"+
		"-,\3\2\2\2-.\3\2\2\2./\3\2\2\2/\61\5\36\20\2\60\62\5\"\22\2\61\60\3\2"+
		"\2\2\61\62\3\2\2\2\62\5\3\2\2\2\63\64\5$\23\2\64\65\7\3\2\2\65\7\3\2\2"+
		"\2\66\67\5$\23\2\67\t\3\2\2\289\7\4\2\29:\5\b\5\2:;\7\5\2\2;\13\3\2\2"+
		"\2<>\5\n\6\2=<\3\2\2\2=>\3\2\2\2>?\3\2\2\2?@\7\6\2\2@\r\3\2\2\2AC\7\7"+
		"\2\2BD\5\n\6\2CB\3\2\2\2CD\3\2\2\2D\17\3\2\2\2EF\5$\23\2F\21\3\2\2\2G"+
		"H\5$\23\2H\23\3\2\2\2IJ\5$\23\2J\25\3\2\2\2KL\5\20\t\2LM\7\b\2\2MN\5\22"+
		"\n\2N\27\3\2\2\2OR\5\24\13\2PQ\7\t\2\2QS\5\26\f\2RP\3\2\2\2RS\3\2\2\2"+
		"S\31\3\2\2\2TU\5$\23\2U\33\3\2\2\2VW\7\n\2\2WX\5\32\16\2XY\7\13\2\2Y\35"+
		"\3\2\2\2Z\\\5\34\17\2[Z\3\2\2\2[\\\3\2\2\2\\]\3\2\2\2]^\5\30\r\2^\37\3"+
		"\2\2\2_`\b\21\1\2`a\5\36\20\2ab\5\f\7\2bi\3\2\2\2cd\f\4\2\2de\5\36\20"+
		"\2ef\5\f\7\2fh\3\2\2\2gc\3\2\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2\2\2j!\3\2\2"+
		"\2ki\3\2\2\2lm\5\16\b\2mn\5\36\20\2no\5\"\22\2ot\3\2\2\2pq\5\16\b\2qr"+
		"\5\36\20\2rt\3\2\2\2sl\3\2\2\2sp\3\2\2\2t#\3\2\2\2u{\7\f\2\2vx\7\r\2\2"+
		"wv\3\2\2\2wx\3\2\2\2xy\3\2\2\2y{\7\16\2\2zu\3\2\2\2zw\3\2\2\2{%\3\2\2"+
		"\2\r*-\61=CR[iswz";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}