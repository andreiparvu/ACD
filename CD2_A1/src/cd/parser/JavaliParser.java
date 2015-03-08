// $ANTLR 3.5.2 /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g 2015-03-04 16:03:04

package cd.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


/**************************************************************************************************
 *   JAVALI PARSER GRAMMAR
 *   Compiler Construction I
 *
 *   ANTLR TIPS AND TRICKS:
 *   -----------------------
 *   
 *   Overview:
 *   ----------
 *
 *   ANTLR is a top-down (LL) parser generator. Given a grammar file
 *   (*.g), ANTLR generates a parser accepting programs written in the
 *   language specified by the grammar. By default, the generated
 *   parser is a Java program.
 *
 *   An ANTLR grammar file (*.g) consists of several sections. In this
 *   grammar file we make use of the following sections:
 *
 *   (1) option declarations
 *   (2) token declarations
 *   (3) action declarations
 *   (4) rule declarations
 *
 *   The rule section must appear at the end. Options (1) alter the
 *   way ANTLR generates code. In this grammar file we use the option
 *   'output=AST', which allows us to apply rewriting rules for AST
 *   construction. The token declaration section (2) declares
 *   tokens. For each declared token, ANTLR generates a corresponding
 *   ANTLR AST node. Rules (4) are either lexer rules (4-a) or parser
 *   rules (4-b).  Lexer rules (4-a) must begin with an upper case
 *   letter and describe tokens that occur in the input stream.
 *   Parser rules (4-b) must begin with a lower case letter and
 *   describe grammar productions.  For each rule in a grammar file,
 *   ANTLR generates a corresponding method in the generated
 *   parser. Using the action declaration section (3), additional
 *   fields and methods can be declared that ANTLR inserts into the
 *   generated parser.
 *
 *   In this assignment, we provide the necessary option (1) and
 *   action (3) declarations, however, you must provide the
 *   appropriate token (2) and rule (4) declarations.
 *
 *   Imaginary tokens:
 *   ---------------
 *
 *   Using the tokens{...} specification, either imaginary tokens can
 *   be defined or aliases for token literals can be defined. An
 *   imaginary token is not associated with a particular input
 *   character (token literal) but is helpful for AST construction. An
 *   imaginary token can be assigned the line and column information
 *   from a token literal appearing in the input. The text of an
 *   imaginary token can be explicitly set to avoid copying the text
 *   from the literal token. E.g., in below example, an imaginary
 *   token ARGUMENTS is created and associated with the actual token
 *   '('. As a result, the imaginary token gets the line and column
 *   number information of the token '(', but hast the text
 *   "ARGUMENTS".
 *
 *   arguments
 *	:	lc='(' expressionlist? ')'
 *		-> ^(ARGUMENTS[$lc,"ARGUMENTS"] expressionlist?)
 *	;
 *
 *   Rules:
 *   ------
 *
 *   ANTLR supports EBNF. The following operators have the following meaning:
 *
 *   x?  x occurs at most once
 *   x*  x occurs zero or several times (left-associative)
 *   x+  x occurs once or several times (left-associative)
 *
 *   Note: Parentheses must be used to apply an operator to a group of tokens!
 *
 *
 *   Left-Associativity:   a op b op c = ( a op b ) op c
 *   Can in principle be achieved  using left recursion:
 *   E ::= E op T | T
 *   However, left-recursive rules are not accepted by ANTLR (see below)! Thus, left
 *   recursion must either be eliminated or, preferably, the EBNF repetition operator
 *   should be used:
 *   E ::= T ( op T)*
 *
 *   Right-Associativity:   a op b op c = a op ( b op c)
 *   Can be achieved using right recursion:
 *   E ::= T op E | T
 *   Above rule might have to be left factored (see below). Preferably, the EBNF '?'
 *   operator should be used:
 *   E ::= T ( op E )?
 *
 *   Left recursion, non-LL(*) decisions, and left-factoring:
 *   ----------------------------------------------
 *   
 *   Being a top-down parser generator, ANTLR cannot deal with
 *   left-recursive rules. As a consequence, any left recursion must
 *   be eliminated.
 *
 *   In a top-down parser, a production alternative must be selected
 *   based on the tokens seen next in the input.  In ANTLR, the number
 *   of tokens used for the decision can be explicitly indicated
 *   (LL(k)) or, alternatively, a variable lookahead (LL(*)) can be
 *   used. For this assignment, we use a variable lookahead. The
 *   selection of a production alternative based on a number k of
 *   lookahead tokens can generally be done by a deterministic finite
 *   automaton (DFA) that recognizes strings of length k and that has
 *   accepting states for each alternative in question. A variable
 *   number of tokens lookahead (LL(*)) can be supported by allowing
 *   cyclic DFAs.
 *
 *   Although LL(*) is clearly superior to LL(k) and therefore accepts
 *   grammars that would not be accepted using a fixed number of
 *   lookahead tokens, there may still be cases for which ANTLR
 *   reports that a rule has "non-LL(*) decisions" even if the grammar
 *   is not ambiguous. This can be the case if the lookahead language
 *   is not regular (i.e., cannot be recognized by a DFA) and/or if
 *   ANTLR does not succeed constructing the DFA for the lookahead due
 *   to recursive (i.e., repetitive) constructs. ANTLR is actually
 *   only capable of constructing the DFA for the lookahead language
 *   in case of recursive constructs as long as only one alternative
 *   is recursive and as long as the internal recursion overflow
 *   constant is sufficiently large (see ANTLR book, page 271). The
 *   only remedy for non-LL(*) decisions is grammar left-factoring.
 *
 *   For example, the grammar below uses non-regular constructs
 *   (nested parentheses) and can therefore not be parsed using the
 *   LL(*) option.
 *
 *   se  =  e '%'  |  e '!' ;
 *   e   =  '(' e ')' | ID ;
 *
 *   In below example, the fact that both alternatives in s are
 *   (indirectly) recursive causes troubles.
 *
 *   s  =  label ID '=' expr
 *      |  label 'return' expr  ;
 *
 *   label = ID ':' label  |  ;
 *
 *   If above grammar is rewritten to use the EBNF looping syntax
 *   instead, however, ANTLR is capable of identifying the looping
 *   construct and constructing the cyclic DFA.
 *
 *   s  =  label ID '=' expr
 *      |  label 'return' expr  ;
 *
 *   label = ( ID ':' )*  ;
 *
 *   And in the following example, finally, ANTLR is capable of
 *   constructing the corresponding DFA for the lookahead language as
 *   only one alternative is recursive and as the looping analysis
 *   recurses less often than specified by the recursion overflow
 *   threshold (see ANTLR book, p. 271):
 *
 *   a  = L a R
 *      | L L X  ;
 *
**************************************************************************************************/
@SuppressWarnings("all")
public class JavaliParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ArrayType", "Assign", "B_AND", 
		"B_DIV", "B_EQUAL", "B_GREATER_OR_EQUAL", "B_GREATER_THAN", "B_LESS_OR_EQUAL", 
		"B_LESS_THAN", "B_MINUS", "B_MOD", "B_NOT_EQUAL", "B_OR", "B_PLUS", "B_TIMES", 
		"BinaryOp", "BooleanConst", "BooleanLiteral", "BuiltInRead", "BuiltInReadFloat", 
		"BuiltInWrite", "BuiltInWriteFloat", "BuiltInWriteln", "COMMENT", "Cast", 
		"ClassDecl", "DecimalIntConst", "DecimalNumber", "DigitNumber", "Field", 
		"FloatConst", "FloatNumber", "HexDigit", "HexIntConst", "HexNumber", "HexPrefix", 
		"Identifier", "IfElse", "Index", "JavaIDDigit", "LINE_COMMENT", "Letter", 
		"MethodBody", "MethodCall", "MethodDecl", "NewArray", "NewObject", "Nop", 
		"NullConst", "ReturnStmt", "Seq", "ThisRef", "U_BOOL_NOT", "U_MINUS", 
		"U_PLUS", "UnaryOp", "Var", "VarDecl", "VarDeclList", "WS", "WhileLoop", 
		"'!'", "'!='", "'%'", "'&&'", "'('", "')'", "'*'", "'+'", "','", "'-'", 
		"'.'", "'/'", "';'", "'<'", "'<='", "'='", "'=='", "'>'", "'>='", "'['", 
		"']'", "'boolean'", "'class'", "'else'", "'extends'", "'float'", "'if'", 
		"'int'", "'new'", "'null'", "'read'", "'readf'", "'return'", "'this'", 
		"'void'", "'while'", "'write'", "'writef'", "'writeln'", "'{'", "'||'", 
		"'}'"
	};
	public static final int EOF=-1;
	public static final int T__65=65;
	public static final int T__66=66;
	public static final int T__67=67;
	public static final int T__68=68;
	public static final int T__69=69;
	public static final int T__70=70;
	public static final int T__71=71;
	public static final int T__72=72;
	public static final int T__73=73;
	public static final int T__74=74;
	public static final int T__75=75;
	public static final int T__76=76;
	public static final int T__77=77;
	public static final int T__78=78;
	public static final int T__79=79;
	public static final int T__80=80;
	public static final int T__81=81;
	public static final int T__82=82;
	public static final int T__83=83;
	public static final int T__84=84;
	public static final int T__85=85;
	public static final int T__86=86;
	public static final int T__87=87;
	public static final int T__88=88;
	public static final int T__89=89;
	public static final int T__90=90;
	public static final int T__91=91;
	public static final int T__92=92;
	public static final int T__93=93;
	public static final int T__94=94;
	public static final int T__95=95;
	public static final int T__96=96;
	public static final int T__97=97;
	public static final int T__98=98;
	public static final int T__99=99;
	public static final int T__100=100;
	public static final int T__101=101;
	public static final int T__102=102;
	public static final int T__103=103;
	public static final int T__104=104;
	public static final int T__105=105;
	public static final int T__106=106;
	public static final int ArrayType=4;
	public static final int Assign=5;
	public static final int B_AND=6;
	public static final int B_DIV=7;
	public static final int B_EQUAL=8;
	public static final int B_GREATER_OR_EQUAL=9;
	public static final int B_GREATER_THAN=10;
	public static final int B_LESS_OR_EQUAL=11;
	public static final int B_LESS_THAN=12;
	public static final int B_MINUS=13;
	public static final int B_MOD=14;
	public static final int B_NOT_EQUAL=15;
	public static final int B_OR=16;
	public static final int B_PLUS=17;
	public static final int B_TIMES=18;
	public static final int BinaryOp=19;
	public static final int BooleanConst=20;
	public static final int BooleanLiteral=21;
	public static final int BuiltInRead=22;
	public static final int BuiltInReadFloat=23;
	public static final int BuiltInWrite=24;
	public static final int BuiltInWriteFloat=25;
	public static final int BuiltInWriteln=26;
	public static final int COMMENT=27;
	public static final int Cast=28;
	public static final int ClassDecl=29;
	public static final int DecimalIntConst=30;
	public static final int DecimalNumber=31;
	public static final int DigitNumber=32;
	public static final int Field=33;
	public static final int FloatConst=34;
	public static final int FloatNumber=35;
	public static final int HexDigit=36;
	public static final int HexIntConst=37;
	public static final int HexNumber=38;
	public static final int HexPrefix=39;
	public static final int Identifier=40;
	public static final int IfElse=41;
	public static final int Index=42;
	public static final int JavaIDDigit=43;
	public static final int LINE_COMMENT=44;
	public static final int Letter=45;
	public static final int MethodBody=46;
	public static final int MethodCall=47;
	public static final int MethodDecl=48;
	public static final int NewArray=49;
	public static final int NewObject=50;
	public static final int Nop=51;
	public static final int NullConst=52;
	public static final int ReturnStmt=53;
	public static final int Seq=54;
	public static final int ThisRef=55;
	public static final int U_BOOL_NOT=56;
	public static final int U_MINUS=57;
	public static final int U_PLUS=58;
	public static final int UnaryOp=59;
	public static final int Var=60;
	public static final int VarDecl=61;
	public static final int VarDeclList=62;
	public static final int WS=63;
	public static final int WhileLoop=64;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public JavaliParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public JavaliParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return JavaliParser.tokenNames; }
	@Override public String getGrammarFileName() { return "/home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g"; }


	protected void mismatch(TokenStream input, int ttype, BitSet follow) throws RecognitionException {
		throw new MismatchedTokenException(ttype, input);
	}

	public void recoverFromMismatchedSet(TokenStream input, RecognitionException e, BitSet follow) throws RecognitionException {
		throw e;
	}

	protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {   
		throw new MismatchedTokenException(ttype, input);
	}   


	public static class unit_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "unit"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:258:1: unit : (declStart= classDecl )+ EOF -> ( classDecl )+ ;
	public final JavaliParser.unit_return unit() throws RecognitionException {
		JavaliParser.unit_return retval = new JavaliParser.unit_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token EOF1=null;
		ParserRuleReturnScope declStart =null;

		Object EOF1_tree=null;
		RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
		RewriteRuleSubtreeStream stream_classDecl=new RewriteRuleSubtreeStream(adaptor,"rule classDecl");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:259:2: ( (declStart= classDecl )+ EOF -> ( classDecl )+ )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:259:4: (declStart= classDecl )+ EOF
			{
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:259:13: (declStart= classDecl )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==87) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:259:13: declStart= classDecl
					{
					pushFollow(FOLLOW_classDecl_in_unit267);
					declStart=classDecl();
					state._fsp--;

					stream_classDecl.add(declStart.getTree());
					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			EOF1=(Token)match(input,EOF,FOLLOW_EOF_in_unit270);  
			stream_EOF.add(EOF1);

			// AST REWRITE
			// elements: classDecl
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 260:3: -> ( classDecl )+
			{
				if ( !(stream_classDecl.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_classDecl.hasNext() ) {
					adaptor.addChild(root_0, stream_classDecl.nextTree());
				}
				stream_classDecl.reset();

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "unit"


	public static class classDecl_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "classDecl"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:263:1: classDecl : (classDeclStart= 'class' Identifier '{' ( declList )? '}' -> ^( ClassDecl[$classDeclStart, \"ClassDecl\"] Identifier Identifier[\"Object\"] ( declList )? ) |classDeclStart= 'class' Identifier 'extends' Identifier '{' ( declList )? '}' -> ^( ClassDecl[$classDeclStart, \"ClassDecl\"] Identifier Identifier ( declList )? ) );
	public final JavaliParser.classDecl_return classDecl() throws RecognitionException {
		JavaliParser.classDecl_return retval = new JavaliParser.classDecl_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token classDeclStart=null;
		Token Identifier2=null;
		Token char_literal3=null;
		Token char_literal5=null;
		Token Identifier6=null;
		Token string_literal7=null;
		Token Identifier8=null;
		Token char_literal9=null;
		Token char_literal11=null;
		ParserRuleReturnScope declList4 =null;
		ParserRuleReturnScope declList10 =null;

		Object classDeclStart_tree=null;
		Object Identifier2_tree=null;
		Object char_literal3_tree=null;
		Object char_literal5_tree=null;
		Object Identifier6_tree=null;
		Object string_literal7_tree=null;
		Object Identifier8_tree=null;
		Object char_literal9_tree=null;
		Object char_literal11_tree=null;
		RewriteRuleTokenStream stream_106=new RewriteRuleTokenStream(adaptor,"token 106");
		RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
		RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
		RewriteRuleTokenStream stream_87=new RewriteRuleTokenStream(adaptor,"token 87");
		RewriteRuleTokenStream stream_89=new RewriteRuleTokenStream(adaptor,"token 89");
		RewriteRuleSubtreeStream stream_declList=new RewriteRuleSubtreeStream(adaptor,"rule declList");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:264:2: (classDeclStart= 'class' Identifier '{' ( declList )? '}' -> ^( ClassDecl[$classDeclStart, \"ClassDecl\"] Identifier Identifier[\"Object\"] ( declList )? ) |classDeclStart= 'class' Identifier 'extends' Identifier '{' ( declList )? '}' -> ^( ClassDecl[$classDeclStart, \"ClassDecl\"] Identifier Identifier ( declList )? ) )
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==87) ) {
				int LA4_1 = input.LA(2);
				if ( (LA4_1==Identifier) ) {
					int LA4_2 = input.LA(3);
					if ( (LA4_2==104) ) {
						alt4=1;
					}
					else if ( (LA4_2==89) ) {
						alt4=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 4, 2, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 4, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}

			switch (alt4) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:264:4: classDeclStart= 'class' Identifier '{' ( declList )? '}'
					{
					classDeclStart=(Token)match(input,87,FOLLOW_87_in_classDecl291);  
					stream_87.add(classDeclStart);

					Identifier2=(Token)match(input,Identifier,FOLLOW_Identifier_in_classDecl293);  
					stream_Identifier.add(Identifier2);

					char_literal3=(Token)match(input,104,FOLLOW_104_in_classDecl295);  
					stream_104.add(char_literal3);

					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:264:42: ( declList )?
					int alt2=2;
					int LA2_0 = input.LA(1);
					if ( (LA2_0==Identifier||LA2_0==86||LA2_0==90||LA2_0==92||LA2_0==99) ) {
						alt2=1;
					}
					switch (alt2) {
						case 1 :
							// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:264:42: declList
							{
							pushFollow(FOLLOW_declList_in_classDecl297);
							declList4=declList();
							state._fsp--;

							stream_declList.add(declList4.getTree());
							}
							break;

					}

					char_literal5=(Token)match(input,106,FOLLOW_106_in_classDecl300);  
					stream_106.add(char_literal5);

					// AST REWRITE
					// elements: declList, Identifier, Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 265:3: -> ^( ClassDecl[$classDeclStart, \"ClassDecl\"] Identifier Identifier[\"Object\"] ( declList )? )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:265:6: ^( ClassDecl[$classDeclStart, \"ClassDecl\"] Identifier Identifier[\"Object\"] ( declList )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ClassDecl, classDeclStart, "ClassDecl"), root_1);
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						adaptor.addChild(root_1, (Object)adaptor.create(Identifier, "Object"));
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:265:81: ( declList )?
						if ( stream_declList.hasNext() ) {
							adaptor.addChild(root_1, stream_declList.nextTree());
						}
						stream_declList.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:266:4: classDeclStart= 'class' Identifier 'extends' Identifier '{' ( declList )? '}'
					{
					classDeclStart=(Token)match(input,87,FOLLOW_87_in_classDecl328);  
					stream_87.add(classDeclStart);

					Identifier6=(Token)match(input,Identifier,FOLLOW_Identifier_in_classDecl330);  
					stream_Identifier.add(Identifier6);

					string_literal7=(Token)match(input,89,FOLLOW_89_in_classDecl332);  
					stream_89.add(string_literal7);

					Identifier8=(Token)match(input,Identifier,FOLLOW_Identifier_in_classDecl334);  
					stream_Identifier.add(Identifier8);

					char_literal9=(Token)match(input,104,FOLLOW_104_in_classDecl336);  
					stream_104.add(char_literal9);

					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:266:63: ( declList )?
					int alt3=2;
					int LA3_0 = input.LA(1);
					if ( (LA3_0==Identifier||LA3_0==86||LA3_0==90||LA3_0==92||LA3_0==99) ) {
						alt3=1;
					}
					switch (alt3) {
						case 1 :
							// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:266:63: declList
							{
							pushFollow(FOLLOW_declList_in_classDecl338);
							declList10=declList();
							state._fsp--;

							stream_declList.add(declList10.getTree());
							}
							break;

					}

					char_literal11=(Token)match(input,106,FOLLOW_106_in_classDecl341);  
					stream_106.add(char_literal11);

					// AST REWRITE
					// elements: Identifier, Identifier, declList
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 267:3: -> ^( ClassDecl[$classDeclStart, \"ClassDecl\"] Identifier Identifier ( declList )? )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:267:6: ^( ClassDecl[$classDeclStart, \"ClassDecl\"] Identifier Identifier ( declList )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ClassDecl, classDeclStart, "ClassDecl"), root_1);
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:267:71: ( declList )?
						if ( stream_declList.hasNext() ) {
							adaptor.addChild(root_1, stream_declList.nextTree());
						}
						stream_declList.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "classDecl"


	public static class declList_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "declList"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:270:1: declList : ( varDecl | methodDecl )+ ;
	public final JavaliParser.declList_return declList() throws RecognitionException {
		JavaliParser.declList_return retval = new JavaliParser.declList_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope varDecl12 =null;
		ParserRuleReturnScope methodDecl13 =null;


		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:271:2: ( ( varDecl | methodDecl )+ )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:271:4: ( varDecl | methodDecl )+
			{
			root_0 = (Object)adaptor.nil();


			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:271:4: ( varDecl | methodDecl )+
			int cnt5=0;
			loop5:
			while (true) {
				int alt5=3;
				switch ( input.LA(1) ) {
				case Identifier:
					{
					int LA5_2 = input.LA(2);
					if ( (LA5_2==84) ) {
						int LA5_7 = input.LA(3);
						if ( (LA5_7==85) ) {
							int LA5_10 = input.LA(4);
							if ( (LA5_10==Identifier) ) {
								int LA5_8 = input.LA(5);
								if ( (LA5_8==73||LA5_8==77) ) {
									alt5=1;
								}
								else if ( (LA5_8==69) ) {
									alt5=2;
								}

							}

						}

					}
					else if ( (LA5_2==Identifier) ) {
						int LA5_8 = input.LA(3);
						if ( (LA5_8==73||LA5_8==77) ) {
							alt5=1;
						}
						else if ( (LA5_8==69) ) {
							alt5=2;
						}

					}

					}
					break;
				case 92:
					{
					int LA5_3 = input.LA(2);
					if ( (LA5_3==Identifier) ) {
						int LA5_8 = input.LA(3);
						if ( (LA5_8==73||LA5_8==77) ) {
							alt5=1;
						}
						else if ( (LA5_8==69) ) {
							alt5=2;
						}

					}
					else if ( (LA5_3==84) ) {
						int LA5_9 = input.LA(3);
						if ( (LA5_9==85) ) {
							int LA5_12 = input.LA(4);
							if ( (LA5_12==Identifier) ) {
								int LA5_8 = input.LA(5);
								if ( (LA5_8==73||LA5_8==77) ) {
									alt5=1;
								}
								else if ( (LA5_8==69) ) {
									alt5=2;
								}

							}

						}

					}

					}
					break;
				case 90:
					{
					int LA5_4 = input.LA(2);
					if ( (LA5_4==Identifier) ) {
						int LA5_8 = input.LA(3);
						if ( (LA5_8==73||LA5_8==77) ) {
							alt5=1;
						}
						else if ( (LA5_8==69) ) {
							alt5=2;
						}

					}
					else if ( (LA5_4==84) ) {
						int LA5_9 = input.LA(3);
						if ( (LA5_9==85) ) {
							int LA5_12 = input.LA(4);
							if ( (LA5_12==Identifier) ) {
								int LA5_8 = input.LA(5);
								if ( (LA5_8==73||LA5_8==77) ) {
									alt5=1;
								}
								else if ( (LA5_8==69) ) {
									alt5=2;
								}

							}

						}

					}

					}
					break;
				case 86:
					{
					int LA5_5 = input.LA(2);
					if ( (LA5_5==Identifier) ) {
						int LA5_8 = input.LA(3);
						if ( (LA5_8==73||LA5_8==77) ) {
							alt5=1;
						}
						else if ( (LA5_8==69) ) {
							alt5=2;
						}

					}
					else if ( (LA5_5==84) ) {
						int LA5_9 = input.LA(3);
						if ( (LA5_9==85) ) {
							int LA5_12 = input.LA(4);
							if ( (LA5_12==Identifier) ) {
								int LA5_8 = input.LA(5);
								if ( (LA5_8==73||LA5_8==77) ) {
									alt5=1;
								}
								else if ( (LA5_8==69) ) {
									alt5=2;
								}

							}

						}

					}

					}
					break;
				case 99:
					{
					alt5=2;
					}
					break;
				}
				switch (alt5) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:271:6: varDecl
					{
					pushFollow(FOLLOW_varDecl_in_declList372);
					varDecl12=varDecl();
					state._fsp--;

					adaptor.addChild(root_0, varDecl12.getTree());

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:271:16: methodDecl
					{
					pushFollow(FOLLOW_methodDecl_in_declList376);
					methodDecl13=methodDecl();
					state._fsp--;

					adaptor.addChild(root_0, methodDecl13.getTree());

					}
					break;

				default :
					if ( cnt5 >= 1 ) break loop5;
					EarlyExitException eee = new EarlyExitException(5, input);
					throw eee;
				}
				cnt5++;
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "declList"


	public static class varDecl_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "varDecl"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:274:1: varDecl : (declStart= type Identifier ';' -> ^( VarDecl[$declStart.start, \"VarDecl\"] type Identifier ) |declStart= type Identifier ( ',' Identifier )+ ';' -> ^( VarDeclList[$declStart.start, \"VarDeclList\"] type ( Identifier )+ ) );
	public final JavaliParser.varDecl_return varDecl() throws RecognitionException {
		JavaliParser.varDecl_return retval = new JavaliParser.varDecl_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token Identifier14=null;
		Token char_literal15=null;
		Token Identifier16=null;
		Token char_literal17=null;
		Token Identifier18=null;
		Token char_literal19=null;
		ParserRuleReturnScope declStart =null;

		Object Identifier14_tree=null;
		Object char_literal15_tree=null;
		Object Identifier16_tree=null;
		Object char_literal17_tree=null;
		Object Identifier18_tree=null;
		Object char_literal19_tree=null;
		RewriteRuleTokenStream stream_77=new RewriteRuleTokenStream(adaptor,"token 77");
		RewriteRuleTokenStream stream_73=new RewriteRuleTokenStream(adaptor,"token 73");
		RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
		RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:275:2: (declStart= type Identifier ';' -> ^( VarDecl[$declStart.start, \"VarDecl\"] type Identifier ) |declStart= type Identifier ( ',' Identifier )+ ';' -> ^( VarDeclList[$declStart.start, \"VarDeclList\"] type ( Identifier )+ ) )
			int alt7=2;
			switch ( input.LA(1) ) {
			case 92:
				{
				int LA7_1 = input.LA(2);
				if ( (LA7_1==Identifier) ) {
					int LA7_5 = input.LA(3);
					if ( (LA7_5==77) ) {
						alt7=1;
					}
					else if ( (LA7_5==73) ) {
						alt7=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 7, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA7_1==84) ) {
					int LA7_6 = input.LA(3);
					if ( (LA7_6==85) ) {
						int LA7_10 = input.LA(4);
						if ( (LA7_10==Identifier) ) {
							int LA7_5 = input.LA(5);
							if ( (LA7_5==77) ) {
								alt7=1;
							}
							else if ( (LA7_5==73) ) {
								alt7=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 7, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 7, 10, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 7, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 7, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 90:
				{
				int LA7_2 = input.LA(2);
				if ( (LA7_2==Identifier) ) {
					int LA7_5 = input.LA(3);
					if ( (LA7_5==77) ) {
						alt7=1;
					}
					else if ( (LA7_5==73) ) {
						alt7=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 7, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA7_2==84) ) {
					int LA7_6 = input.LA(3);
					if ( (LA7_6==85) ) {
						int LA7_10 = input.LA(4);
						if ( (LA7_10==Identifier) ) {
							int LA7_5 = input.LA(5);
							if ( (LA7_5==77) ) {
								alt7=1;
							}
							else if ( (LA7_5==73) ) {
								alt7=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 7, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 7, 10, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 7, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 7, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 86:
				{
				int LA7_3 = input.LA(2);
				if ( (LA7_3==Identifier) ) {
					int LA7_5 = input.LA(3);
					if ( (LA7_5==77) ) {
						alt7=1;
					}
					else if ( (LA7_5==73) ) {
						alt7=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 7, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA7_3==84) ) {
					int LA7_6 = input.LA(3);
					if ( (LA7_6==85) ) {
						int LA7_10 = input.LA(4);
						if ( (LA7_10==Identifier) ) {
							int LA7_5 = input.LA(5);
							if ( (LA7_5==77) ) {
								alt7=1;
							}
							else if ( (LA7_5==73) ) {
								alt7=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 7, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 7, 10, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 7, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 7, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case Identifier:
				{
				int LA7_4 = input.LA(2);
				if ( (LA7_4==84) ) {
					int LA7_7 = input.LA(3);
					if ( (LA7_7==85) ) {
						int LA7_11 = input.LA(4);
						if ( (LA7_11==Identifier) ) {
							int LA7_5 = input.LA(5);
							if ( (LA7_5==77) ) {
								alt7=1;
							}
							else if ( (LA7_5==73) ) {
								alt7=2;
							}

							else {
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 7, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 7, 11, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 7, 7, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA7_4==Identifier) ) {
					int LA7_5 = input.LA(3);
					if ( (LA7_5==77) ) {
						alt7=1;
					}
					else if ( (LA7_5==73) ) {
						alt7=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 7, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 7, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}
			switch (alt7) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:275:4: declStart= type Identifier ';'
					{
					pushFollow(FOLLOW_type_in_varDecl392);
					declStart=type();
					state._fsp--;

					stream_type.add(declStart.getTree());
					Identifier14=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDecl394);  
					stream_Identifier.add(Identifier14);

					char_literal15=(Token)match(input,77,FOLLOW_77_in_varDecl396);  
					stream_77.add(char_literal15);

					// AST REWRITE
					// elements: type, Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 276:3: -> ^( VarDecl[$declStart.start, \"VarDecl\"] type Identifier )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:276:6: ^( VarDecl[$declStart.start, \"VarDecl\"] type Identifier )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(VarDecl, (declStart!=null?(declStart.start):null), "VarDecl"), root_1);
						adaptor.addChild(root_1, stream_type.nextTree());
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:277:4: declStart= type Identifier ( ',' Identifier )+ ';'
					{
					pushFollow(FOLLOW_type_in_varDecl418);
					declStart=type();
					state._fsp--;

					stream_type.add(declStart.getTree());
					Identifier16=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDecl420);  
					stream_Identifier.add(Identifier16);

					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:277:30: ( ',' Identifier )+
					int cnt6=0;
					loop6:
					while (true) {
						int alt6=2;
						int LA6_0 = input.LA(1);
						if ( (LA6_0==73) ) {
							alt6=1;
						}

						switch (alt6) {
						case 1 :
							// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:277:32: ',' Identifier
							{
							char_literal17=(Token)match(input,73,FOLLOW_73_in_varDecl424);  
							stream_73.add(char_literal17);

							Identifier18=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDecl426);  
							stream_Identifier.add(Identifier18);

							}
							break;

						default :
							if ( cnt6 >= 1 ) break loop6;
							EarlyExitException eee = new EarlyExitException(6, input);
							throw eee;
						}
						cnt6++;
					}

					char_literal19=(Token)match(input,77,FOLLOW_77_in_varDecl431);  
					stream_77.add(char_literal19);

					// AST REWRITE
					// elements: Identifier, type
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 278:3: -> ^( VarDeclList[$declStart.start, \"VarDeclList\"] type ( Identifier )+ )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:278:6: ^( VarDeclList[$declStart.start, \"VarDeclList\"] type ( Identifier )+ )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(VarDeclList, (declStart!=null?(declStart.start):null), "VarDeclList"), root_1);
						adaptor.addChild(root_1, stream_type.nextTree());
						if ( !(stream_Identifier.hasNext()) ) {
							throw new RewriteEarlyExitException();
						}
						while ( stream_Identifier.hasNext() ) {
							adaptor.addChild(root_1, stream_Identifier.nextNode());
						}
						stream_Identifier.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "varDecl"


	public static class methodDecl_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "methodDecl"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:281:1: methodDecl : methSig= methodHeading methodBody -> ^( MethodDecl[$methSig.start, \"MethodDecl\"] methodHeading methodBody ) ;
	public final JavaliParser.methodDecl_return methodDecl() throws RecognitionException {
		JavaliParser.methodDecl_return retval = new JavaliParser.methodDecl_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope methSig =null;
		ParserRuleReturnScope methodBody20 =null;

		RewriteRuleSubtreeStream stream_methodBody=new RewriteRuleSubtreeStream(adaptor,"rule methodBody");
		RewriteRuleSubtreeStream stream_methodHeading=new RewriteRuleSubtreeStream(adaptor,"rule methodHeading");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:282:2: (methSig= methodHeading methodBody -> ^( MethodDecl[$methSig.start, \"MethodDecl\"] methodHeading methodBody ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:282:4: methSig= methodHeading methodBody
			{
			pushFollow(FOLLOW_methodHeading_in_methodDecl460);
			methSig=methodHeading();
			state._fsp--;

			stream_methodHeading.add(methSig.getTree());
			pushFollow(FOLLOW_methodBody_in_methodDecl462);
			methodBody20=methodBody();
			state._fsp--;

			stream_methodBody.add(methodBody20.getTree());
			// AST REWRITE
			// elements: methodBody, methodHeading
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 283:3: -> ^( MethodDecl[$methSig.start, \"MethodDecl\"] methodHeading methodBody )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:283:6: ^( MethodDecl[$methSig.start, \"MethodDecl\"] methodHeading methodBody )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MethodDecl, (methSig!=null?(methSig.start):null), "MethodDecl"), root_1);
				adaptor.addChild(root_1, stream_methodHeading.nextTree());
				adaptor.addChild(root_1, stream_methodBody.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "methodDecl"


	public static class methodHeading_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "methodHeading"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:286:1: methodHeading : ( type Identifier '(' ( formalParamList )? ')' -> type Identifier ( formalParamList )? |methSig= 'void' Identifier '(' ( formalParamList )? ')' -> Identifier[$methSig, \"void\"] Identifier ( formalParamList )? );
	public final JavaliParser.methodHeading_return methodHeading() throws RecognitionException {
		JavaliParser.methodHeading_return retval = new JavaliParser.methodHeading_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token methSig=null;
		Token Identifier22=null;
		Token char_literal23=null;
		Token char_literal25=null;
		Token Identifier26=null;
		Token char_literal27=null;
		Token char_literal29=null;
		ParserRuleReturnScope type21 =null;
		ParserRuleReturnScope formalParamList24 =null;
		ParserRuleReturnScope formalParamList28 =null;

		Object methSig_tree=null;
		Object Identifier22_tree=null;
		Object char_literal23_tree=null;
		Object char_literal25_tree=null;
		Object Identifier26_tree=null;
		Object char_literal27_tree=null;
		Object char_literal29_tree=null;
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");
		RewriteRuleTokenStream stream_99=new RewriteRuleTokenStream(adaptor,"token 99");
		RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
		RewriteRuleSubtreeStream stream_formalParamList=new RewriteRuleSubtreeStream(adaptor,"rule formalParamList");
		RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:287:4: ( type Identifier '(' ( formalParamList )? ')' -> type Identifier ( formalParamList )? |methSig= 'void' Identifier '(' ( formalParamList )? ')' -> Identifier[$methSig, \"void\"] Identifier ( formalParamList )? )
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==Identifier||LA10_0==86||LA10_0==90||LA10_0==92) ) {
				alt10=1;
			}
			else if ( (LA10_0==99) ) {
				alt10=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}

			switch (alt10) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:287:6: type Identifier '(' ( formalParamList )? ')'
					{
					pushFollow(FOLLOW_type_in_methodHeading491);
					type21=type();
					state._fsp--;

					stream_type.add(type21.getTree());
					Identifier22=(Token)match(input,Identifier,FOLLOW_Identifier_in_methodHeading493);  
					stream_Identifier.add(Identifier22);

					char_literal23=(Token)match(input,69,FOLLOW_69_in_methodHeading495);  
					stream_69.add(char_literal23);

					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:287:26: ( formalParamList )?
					int alt8=2;
					int LA8_0 = input.LA(1);
					if ( (LA8_0==Identifier||LA8_0==86||LA8_0==90||LA8_0==92) ) {
						alt8=1;
					}
					switch (alt8) {
						case 1 :
							// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:287:26: formalParamList
							{
							pushFollow(FOLLOW_formalParamList_in_methodHeading497);
							formalParamList24=formalParamList();
							state._fsp--;

							stream_formalParamList.add(formalParamList24.getTree());
							}
							break;

					}

					char_literal25=(Token)match(input,70,FOLLOW_70_in_methodHeading500);  
					stream_70.add(char_literal25);

					// AST REWRITE
					// elements: formalParamList, Identifier, type
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 288:7: -> type Identifier ( formalParamList )?
					{
						adaptor.addChild(root_0, stream_type.nextTree());
						adaptor.addChild(root_0, stream_Identifier.nextNode());
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:288:26: ( formalParamList )?
						if ( stream_formalParamList.hasNext() ) {
							adaptor.addChild(root_0, stream_formalParamList.nextTree());
						}
						stream_formalParamList.reset();

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:289:6: methSig= 'void' Identifier '(' ( formalParamList )? ')'
					{
					methSig=(Token)match(input,99,FOLLOW_99_in_methodHeading524);  
					stream_99.add(methSig);

					Identifier26=(Token)match(input,Identifier,FOLLOW_Identifier_in_methodHeading526);  
					stream_Identifier.add(Identifier26);

					char_literal27=(Token)match(input,69,FOLLOW_69_in_methodHeading528);  
					stream_69.add(char_literal27);

					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:289:36: ( formalParamList )?
					int alt9=2;
					int LA9_0 = input.LA(1);
					if ( (LA9_0==Identifier||LA9_0==86||LA9_0==90||LA9_0==92) ) {
						alt9=1;
					}
					switch (alt9) {
						case 1 :
							// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:289:36: formalParamList
							{
							pushFollow(FOLLOW_formalParamList_in_methodHeading530);
							formalParamList28=formalParamList();
							state._fsp--;

							stream_formalParamList.add(formalParamList28.getTree());
							}
							break;

					}

					char_literal29=(Token)match(input,70,FOLLOW_70_in_methodHeading533);  
					stream_70.add(char_literal29);

					// AST REWRITE
					// elements: Identifier, formalParamList, Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 290:7: -> Identifier[$methSig, \"void\"] Identifier ( formalParamList )?
					{
						adaptor.addChild(root_0, (Object)adaptor.create(Identifier, methSig, "void"));
						adaptor.addChild(root_0, stream_Identifier.nextNode());
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:290:50: ( formalParamList )?
						if ( stream_formalParamList.hasNext() ) {
							adaptor.addChild(root_0, stream_formalParamList.nextTree());
						}
						stream_formalParamList.reset();

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "methodHeading"


	public static class formalParamList_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "formalParamList"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:293:1: formalParamList : paramDecl= type Identifier ( ',' type Identifier )* -> ( ^( VarDecl[$paramDecl.start, \"VarDecl\"] type Identifier ) )+ ;
	public final JavaliParser.formalParamList_return formalParamList() throws RecognitionException {
		JavaliParser.formalParamList_return retval = new JavaliParser.formalParamList_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token Identifier30=null;
		Token char_literal31=null;
		Token Identifier33=null;
		ParserRuleReturnScope paramDecl =null;
		ParserRuleReturnScope type32 =null;

		Object Identifier30_tree=null;
		Object char_literal31_tree=null;
		Object Identifier33_tree=null;
		RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
		RewriteRuleTokenStream stream_73=new RewriteRuleTokenStream(adaptor,"token 73");
		RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:294:2: (paramDecl= type Identifier ( ',' type Identifier )* -> ( ^( VarDecl[$paramDecl.start, \"VarDecl\"] type Identifier ) )+ )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:294:4: paramDecl= type Identifier ( ',' type Identifier )*
			{
			pushFollow(FOLLOW_type_in_formalParamList564);
			paramDecl=type();
			state._fsp--;

			stream_type.add(paramDecl.getTree());
			Identifier30=(Token)match(input,Identifier,FOLLOW_Identifier_in_formalParamList566);  
			stream_Identifier.add(Identifier30);

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:294:30: ( ',' type Identifier )*
			loop11:
			while (true) {
				int alt11=2;
				int LA11_0 = input.LA(1);
				if ( (LA11_0==73) ) {
					alt11=1;
				}

				switch (alt11) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:294:32: ',' type Identifier
					{
					char_literal31=(Token)match(input,73,FOLLOW_73_in_formalParamList570);  
					stream_73.add(char_literal31);

					pushFollow(FOLLOW_type_in_formalParamList572);
					type32=type();
					state._fsp--;

					stream_type.add(type32.getTree());
					Identifier33=(Token)match(input,Identifier,FOLLOW_Identifier_in_formalParamList574);  
					stream_Identifier.add(Identifier33);

					}
					break;

				default :
					break loop11;
				}
			}

			// AST REWRITE
			// elements: Identifier, type
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 295:3: -> ( ^( VarDecl[$paramDecl.start, \"VarDecl\"] type Identifier ) )+
			{
				if ( !(stream_Identifier.hasNext()||stream_type.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_Identifier.hasNext()||stream_type.hasNext() ) {
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:295:6: ^( VarDecl[$paramDecl.start, \"VarDecl\"] type Identifier )
					{
					Object root_1 = (Object)adaptor.nil();
					root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(VarDecl, (paramDecl!=null?(paramDecl.start):null), "VarDecl"), root_1);
					adaptor.addChild(root_1, stream_type.nextTree());
					adaptor.addChild(root_1, stream_Identifier.nextNode());
					adaptor.addChild(root_0, root_1);
					}

				}
				stream_Identifier.reset();
				stream_type.reset();

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "formalParamList"


	public static class methodBody_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "methodBody"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:298:1: methodBody : ( methodBodyWithDeclList (stmtSeq= stmtList '}' -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) ) |rb= '}' -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$rb, \"Seq\"] ) ) ) |lb= '{' stmtSeq= stmtList '}' -> ^( MethodBody[$lb, \"MethodBody\"] ^( Seq[$lb, \"Seq\"] ) ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) ) |lb= '{' '}' -> ^( MethodBody[$lb, \"MethodBody\"] ^( Seq[$lb, \"Seq\"] ) ^( Seq[$lb, \"Seq\"] ) ) );
	public final JavaliParser.methodBody_return methodBody() throws RecognitionException {
		JavaliParser.methodBody_return retval = new JavaliParser.methodBody_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token rb=null;
		Token lb=null;
		Token char_literal35=null;
		Token char_literal36=null;
		Token char_literal37=null;
		ParserRuleReturnScope stmtSeq =null;
		ParserRuleReturnScope methodBodyWithDeclList34 =null;

		Object rb_tree=null;
		Object lb_tree=null;
		Object char_literal35_tree=null;
		Object char_literal36_tree=null;
		Object char_literal37_tree=null;
		RewriteRuleTokenStream stream_106=new RewriteRuleTokenStream(adaptor,"token 106");
		RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
		RewriteRuleSubtreeStream stream_stmtList=new RewriteRuleSubtreeStream(adaptor,"rule stmtList");
		RewriteRuleSubtreeStream stream_methodBodyWithDeclList=new RewriteRuleSubtreeStream(adaptor,"rule methodBodyWithDeclList");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:299:2: ( methodBodyWithDeclList (stmtSeq= stmtList '}' -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) ) |rb= '}' -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$rb, \"Seq\"] ) ) ) |lb= '{' stmtSeq= stmtList '}' -> ^( MethodBody[$lb, \"MethodBody\"] ^( Seq[$lb, \"Seq\"] ) ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) ) |lb= '{' '}' -> ^( MethodBody[$lb, \"MethodBody\"] ^( Seq[$lb, \"Seq\"] ) ^( Seq[$lb, \"Seq\"] ) ) )
			int alt13=3;
			int LA13_0 = input.LA(1);
			if ( (LA13_0==104) ) {
				switch ( input.LA(2) ) {
				case 106:
					{
					alt13=3;
					}
					break;
				case 86:
				case 90:
				case 92:
				case 99:
					{
					alt13=1;
					}
					break;
				case Identifier:
					{
					switch ( input.LA(3) ) {
					case 84:
						{
						int LA13_6 = input.LA(4);
						if ( (LA13_6==85) ) {
							alt13=1;
						}
						else if ( (LA13_6==BooleanLiteral||LA13_6==DecimalNumber||LA13_6==FloatNumber||LA13_6==HexNumber||LA13_6==Identifier||LA13_6==65||LA13_6==69||LA13_6==72||LA13_6==74||LA13_6==94||LA13_6==98) ) {
							alt13=2;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 13, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

						}
						break;
					case Identifier:
						{
						alt13=1;
						}
						break;
					case 69:
					case 75:
					case 77:
					case 80:
						{
						alt13=2;
						}
						break;
					default:
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 13, 4, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case 91:
				case 97:
				case 98:
				case 100:
				case 101:
				case 102:
				case 103:
					{
					alt13=2;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 13, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 13, 0, input);
				throw nvae;
			}

			switch (alt13) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:299:4: methodBodyWithDeclList (stmtSeq= stmtList '}' -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) ) |rb= '}' -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$rb, \"Seq\"] ) ) )
					{
					pushFollow(FOLLOW_methodBodyWithDeclList_in_methodBody604);
					methodBodyWithDeclList34=methodBodyWithDeclList();
					state._fsp--;

					stream_methodBodyWithDeclList.add(methodBodyWithDeclList34.getTree());
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:300:3: (stmtSeq= stmtList '}' -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) ) |rb= '}' -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$rb, \"Seq\"] ) ) )
					int alt12=2;
					int LA12_0 = input.LA(1);
					if ( (LA12_0==Identifier||LA12_0==91||(LA12_0 >= 97 && LA12_0 <= 98)||(LA12_0 >= 100 && LA12_0 <= 103)) ) {
						alt12=1;
					}
					else if ( (LA12_0==106) ) {
						alt12=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 12, 0, input);
						throw nvae;
					}

					switch (alt12) {
						case 1 :
							// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:300:5: stmtSeq= stmtList '}'
							{
							pushFollow(FOLLOW_stmtList_in_methodBody613);
							stmtSeq=stmtList();
							state._fsp--;

							stream_stmtList.add(stmtSeq.getTree());
							char_literal35=(Token)match(input,106,FOLLOW_106_in_methodBody615);  
							stream_106.add(char_literal35);

							// AST REWRITE
							// elements: stmtList, methodBodyWithDeclList
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (Object)adaptor.nil();
							// 301:4: -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) )
							{
								// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:301:7: ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) )
								{
								Object root_1 = (Object)adaptor.nil();
								root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MethodBody, (retval.start), "MethodBody"), root_1);
								adaptor.addChild(root_1, stream_methodBodyWithDeclList.nextTree());
								// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:301:77: ^( Seq[$stmtSeq.start, \"Seq\"] stmtList )
								{
								Object root_2 = (Object)adaptor.nil();
								root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(Seq, (stmtSeq!=null?(stmtSeq.start):null), "Seq"), root_2);
								adaptor.addChild(root_2, stream_stmtList.nextTree());
								adaptor.addChild(root_1, root_2);
								}

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;

							}
							break;
						case 2 :
							// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:302:5: rb= '}'
							{
							rb=(Token)match(input,106,FOLLOW_106_in_methodBody646);  
							stream_106.add(rb);

							// AST REWRITE
							// elements: methodBodyWithDeclList
							// token labels: 
							// rule labels: retval
							// token list labels: 
							// rule list labels: 
							// wildcard labels: 
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (Object)adaptor.nil();
							// 303:4: -> ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$rb, \"Seq\"] ) )
							{
								// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:303:7: ^( MethodBody[$methodBody.start, \"MethodBody\"] methodBodyWithDeclList ^( Seq[$rb, \"Seq\"] ) )
								{
								Object root_1 = (Object)adaptor.nil();
								root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MethodBody, (retval.start), "MethodBody"), root_1);
								adaptor.addChild(root_1, stream_methodBodyWithDeclList.nextTree());
								// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:303:77: ^( Seq[$rb, \"Seq\"] )
								{
								Object root_2 = (Object)adaptor.nil();
								root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(Seq, rb, "Seq"), root_2);
								adaptor.addChild(root_1, root_2);
								}

								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;

							}
							break;

					}

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:305:4: lb= '{' stmtSeq= stmtList '}'
					{
					lb=(Token)match(input,104,FOLLOW_104_in_methodBody678);  
					stream_104.add(lb);

					pushFollow(FOLLOW_stmtList_in_methodBody682);
					stmtSeq=stmtList();
					state._fsp--;

					stream_stmtList.add(stmtSeq.getTree());
					char_literal36=(Token)match(input,106,FOLLOW_106_in_methodBody684);  
					stream_106.add(char_literal36);

					// AST REWRITE
					// elements: stmtList
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 306:3: -> ^( MethodBody[$lb, \"MethodBody\"] ^( Seq[$lb, \"Seq\"] ) ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:306:6: ^( MethodBody[$lb, \"MethodBody\"] ^( Seq[$lb, \"Seq\"] ) ^( Seq[$stmtSeq.start, \"Seq\"] stmtList ) )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MethodBody, lb, "MethodBody"), root_1);
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:306:39: ^( Seq[$lb, \"Seq\"] )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(Seq, lb, "Seq"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:306:60: ^( Seq[$stmtSeq.start, \"Seq\"] stmtList )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(Seq, (stmtSeq!=null?(stmtSeq.start):null), "Seq"), root_2);
						adaptor.addChild(root_2, stream_stmtList.nextTree());
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:307:4: lb= '{' '}'
					{
					lb=(Token)match(input,104,FOLLOW_104_in_methodBody718);  
					stream_104.add(lb);

					char_literal37=(Token)match(input,106,FOLLOW_106_in_methodBody721);  
					stream_106.add(char_literal37);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 308:3: -> ^( MethodBody[$lb, \"MethodBody\"] ^( Seq[$lb, \"Seq\"] ) ^( Seq[$lb, \"Seq\"] ) )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:308:6: ^( MethodBody[$lb, \"MethodBody\"] ^( Seq[$lb, \"Seq\"] ) ^( Seq[$lb, \"Seq\"] ) )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MethodBody, lb, "MethodBody"), root_1);
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:308:39: ^( Seq[$lb, \"Seq\"] )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(Seq, lb, "Seq"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:308:60: ^( Seq[$lb, \"Seq\"] )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(Seq, lb, "Seq"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "methodBody"


	public static class methodBodyWithDeclList_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "methodBodyWithDeclList"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:311:1: methodBodyWithDeclList : lb= '{' declSeq= declList -> ^( Seq[$declSeq.start, \"Seq\"] declList ) ;
	public final JavaliParser.methodBodyWithDeclList_return methodBodyWithDeclList() throws RecognitionException {
		JavaliParser.methodBodyWithDeclList_return retval = new JavaliParser.methodBodyWithDeclList_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token lb=null;
		ParserRuleReturnScope declSeq =null;

		Object lb_tree=null;
		RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
		RewriteRuleSubtreeStream stream_declList=new RewriteRuleSubtreeStream(adaptor,"rule declList");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:312:2: (lb= '{' declSeq= declList -> ^( Seq[$declSeq.start, \"Seq\"] declList ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:312:4: lb= '{' declSeq= declList
			{
			lb=(Token)match(input,104,FOLLOW_104_in_methodBodyWithDeclList759);  
			stream_104.add(lb);

			pushFollow(FOLLOW_declList_in_methodBodyWithDeclList763);
			declSeq=declList();
			state._fsp--;

			stream_declList.add(declSeq.getTree());
			// AST REWRITE
			// elements: declList
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 313:3: -> ^( Seq[$declSeq.start, \"Seq\"] declList )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:313:6: ^( Seq[$declSeq.start, \"Seq\"] declList )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Seq, (declSeq!=null?(declSeq.start):null), "Seq"), root_1);
				adaptor.addChild(root_1, stream_declList.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "methodBodyWithDeclList"


	public static class stmtList_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "stmtList"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:318:1: stmtList : ( stmt )+ -> ( stmt )+ ;
	public final JavaliParser.stmtList_return stmtList() throws RecognitionException {
		JavaliParser.stmtList_return retval = new JavaliParser.stmtList_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope stmt38 =null;

		RewriteRuleSubtreeStream stream_stmt=new RewriteRuleSubtreeStream(adaptor,"rule stmt");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:319:2: ( ( stmt )+ -> ( stmt )+ )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:319:4: ( stmt )+
			{
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:319:4: ( stmt )+
			int cnt14=0;
			loop14:
			while (true) {
				int alt14=2;
				int LA14_0 = input.LA(1);
				if ( (LA14_0==Identifier||LA14_0==91||(LA14_0 >= 97 && LA14_0 <= 98)||(LA14_0 >= 100 && LA14_0 <= 103)) ) {
					alt14=1;
				}

				switch (alt14) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:319:4: stmt
					{
					pushFollow(FOLLOW_stmt_in_stmtList789);
					stmt38=stmt();
					state._fsp--;

					stream_stmt.add(stmt38.getTree());
					}
					break;

				default :
					if ( cnt14 >= 1 ) break loop14;
					EarlyExitException eee = new EarlyExitException(14, input);
					throw eee;
				}
				cnt14++;
			}

			// AST REWRITE
			// elements: stmt
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 320:3: -> ( stmt )+
			{
				if ( !(stream_stmt.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_stmt.hasNext() ) {
					adaptor.addChild(root_0, stream_stmt.nextTree());
				}
				stream_stmt.reset();

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "stmtList"


	public static class stmt_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "stmt"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:323:1: stmt : ( assignmentOrMethodCall ';' -> assignmentOrMethodCall | ioStmt | ifStmt | whileStmt | returnStmt );
	public final JavaliParser.stmt_return stmt() throws RecognitionException {
		JavaliParser.stmt_return retval = new JavaliParser.stmt_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal40=null;
		ParserRuleReturnScope assignmentOrMethodCall39 =null;
		ParserRuleReturnScope ioStmt41 =null;
		ParserRuleReturnScope ifStmt42 =null;
		ParserRuleReturnScope whileStmt43 =null;
		ParserRuleReturnScope returnStmt44 =null;

		Object char_literal40_tree=null;
		RewriteRuleTokenStream stream_77=new RewriteRuleTokenStream(adaptor,"token 77");
		RewriteRuleSubtreeStream stream_assignmentOrMethodCall=new RewriteRuleSubtreeStream(adaptor,"rule assignmentOrMethodCall");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:324:2: ( assignmentOrMethodCall ';' -> assignmentOrMethodCall | ioStmt | ifStmt | whileStmt | returnStmt )
			int alt15=5;
			switch ( input.LA(1) ) {
			case Identifier:
			case 98:
				{
				alt15=1;
				}
				break;
			case 101:
			case 102:
			case 103:
				{
				alt15=2;
				}
				break;
			case 91:
				{
				alt15=3;
				}
				break;
			case 100:
				{
				alt15=4;
				}
				break;
			case 97:
				{
				alt15=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}
			switch (alt15) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:324:4: assignmentOrMethodCall ';'
					{
					pushFollow(FOLLOW_assignmentOrMethodCall_in_stmt808);
					assignmentOrMethodCall39=assignmentOrMethodCall();
					state._fsp--;

					stream_assignmentOrMethodCall.add(assignmentOrMethodCall39.getTree());
					char_literal40=(Token)match(input,77,FOLLOW_77_in_stmt810);  
					stream_77.add(char_literal40);

					// AST REWRITE
					// elements: assignmentOrMethodCall
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 325:3: -> assignmentOrMethodCall
					{
						adaptor.addChild(root_0, stream_assignmentOrMethodCall.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:326:7: ioStmt
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ioStmt_in_stmt824);
					ioStmt41=ioStmt();
					state._fsp--;

					adaptor.addChild(root_0, ioStmt41.getTree());

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:327:4: ifStmt
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_ifStmt_in_stmt829);
					ifStmt42=ifStmt();
					state._fsp--;

					adaptor.addChild(root_0, ifStmt42.getTree());

					}
					break;
				case 4 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:328:4: whileStmt
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_whileStmt_in_stmt834);
					whileStmt43=whileStmt();
					state._fsp--;

					adaptor.addChild(root_0, whileStmt43.getTree());

					}
					break;
				case 5 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:329:5: returnStmt
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_returnStmt_in_stmt840);
					returnStmt44=returnStmt();
					state._fsp--;

					adaptor.addChild(root_0, returnStmt44.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "stmt"


	public static class assignmentOrMethodCall_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "assignmentOrMethodCall"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:333:1: assignmentOrMethodCall : target= identAccess ( assignmentTail[$target.tree] -> assignmentTail | -> identAccess ) ;
	public final JavaliParser.assignmentOrMethodCall_return assignmentOrMethodCall() throws RecognitionException {
		JavaliParser.assignmentOrMethodCall_return retval = new JavaliParser.assignmentOrMethodCall_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope target =null;
		ParserRuleReturnScope assignmentTail45 =null;

		RewriteRuleSubtreeStream stream_identAccess=new RewriteRuleSubtreeStream(adaptor,"rule identAccess");
		RewriteRuleSubtreeStream stream_assignmentTail=new RewriteRuleSubtreeStream(adaptor,"rule assignmentTail");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:334:4: (target= identAccess ( assignmentTail[$target.tree] -> assignmentTail | -> identAccess ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:334:7: target= identAccess ( assignmentTail[$target.tree] -> assignmentTail | -> identAccess )
			{
			pushFollow(FOLLOW_identAccess_in_assignmentOrMethodCall857);
			target=identAccess();
			state._fsp--;

			stream_identAccess.add(target.getTree());
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:335:7: ( assignmentTail[$target.tree] -> assignmentTail | -> identAccess )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==80) ) {
				alt16=1;
			}
			else if ( (LA16_0==77) ) {
				alt16=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:335:10: assignmentTail[$target.tree]
					{
					pushFollow(FOLLOW_assignmentTail_in_assignmentOrMethodCall868);
					assignmentTail45=assignmentTail((target!=null?((Object)target.getTree()):null));
					state._fsp--;

					stream_assignmentTail.add(assignmentTail45.getTree());
					// AST REWRITE
					// elements: assignmentTail
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 336:10: -> assignmentTail
					{
						adaptor.addChild(root_0, stream_assignmentTail.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:338:10: 
					{
					// AST REWRITE
					// elements: identAccess
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 338:10: -> identAccess
					{
						adaptor.addChild(root_0, stream_identAccess.nextTree());
					}


					retval.tree = root_0;

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "assignmentOrMethodCall"


	public static class assignmentTail_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "assignmentTail"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:342:1: assignmentTail[Object lhs] : eq= '=' rightExpr= assignmentRHS -> ^( Assign[$eq, \"Assign\"] assignmentRHS ) ;
	public final JavaliParser.assignmentTail_return assignmentTail(Object lhs) throws RecognitionException {
		JavaliParser.assignmentTail_return retval = new JavaliParser.assignmentTail_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token eq=null;
		ParserRuleReturnScope rightExpr =null;

		Object eq_tree=null;
		RewriteRuleTokenStream stream_80=new RewriteRuleTokenStream(adaptor,"token 80");
		RewriteRuleSubtreeStream stream_assignmentRHS=new RewriteRuleSubtreeStream(adaptor,"rule assignmentRHS");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:343:2: (eq= '=' rightExpr= assignmentRHS -> ^( Assign[$eq, \"Assign\"] assignmentRHS ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:343:4: eq= '=' rightExpr= assignmentRHS
			{
			eq=(Token)match(input,80,FOLLOW_80_in_assignmentTail929);  
			stream_80.add(eq);

			pushFollow(FOLLOW_assignmentRHS_in_assignmentTail933);
			rightExpr=assignmentRHS();
			state._fsp--;

			stream_assignmentRHS.add(rightExpr.getTree());
			// AST REWRITE
			// elements: assignmentRHS
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 344:3: -> ^( Assign[$eq, \"Assign\"] assignmentRHS )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:344:6: ^( Assign[$eq, \"Assign\"] assignmentRHS )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Assign, eq, "Assign"), root_1);
				adaptor.addChild(root_1,  lhs );
				adaptor.addChild(root_1, stream_assignmentRHS.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "assignmentTail"


	public static class assignmentRHS_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "assignmentRHS"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:347:1: assignmentRHS : ( expr | newExpr | readExpr | readExprFloat ) ;
	public final JavaliParser.assignmentRHS_return assignmentRHS() throws RecognitionException {
		JavaliParser.assignmentRHS_return retval = new JavaliParser.assignmentRHS_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope expr46 =null;
		ParserRuleReturnScope newExpr47 =null;
		ParserRuleReturnScope readExpr48 =null;
		ParserRuleReturnScope readExprFloat49 =null;


		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:348:2: ( ( expr | newExpr | readExpr | readExprFloat ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:348:4: ( expr | newExpr | readExpr | readExprFloat )
			{
			root_0 = (Object)adaptor.nil();


			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:348:4: ( expr | newExpr | readExpr | readExprFloat )
			int alt17=4;
			switch ( input.LA(1) ) {
			case BooleanLiteral:
			case DecimalNumber:
			case FloatNumber:
			case HexNumber:
			case Identifier:
			case 65:
			case 69:
			case 72:
			case 74:
			case 94:
			case 98:
				{
				alt17=1;
				}
				break;
			case 93:
				{
				alt17=2;
				}
				break;
			case 95:
				{
				alt17=3;
				}
				break;
			case 96:
				{
				alt17=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 17, 0, input);
				throw nvae;
			}
			switch (alt17) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:348:6: expr
					{
					pushFollow(FOLLOW_expr_in_assignmentRHS961);
					expr46=expr();
					state._fsp--;

					adaptor.addChild(root_0, expr46.getTree());

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:348:13: newExpr
					{
					pushFollow(FOLLOW_newExpr_in_assignmentRHS965);
					newExpr47=newExpr();
					state._fsp--;

					adaptor.addChild(root_0, newExpr47.getTree());

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:348:23: readExpr
					{
					pushFollow(FOLLOW_readExpr_in_assignmentRHS969);
					readExpr48=readExpr();
					state._fsp--;

					adaptor.addChild(root_0, readExpr48.getTree());

					}
					break;
				case 4 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:348:34: readExprFloat
					{
					pushFollow(FOLLOW_readExprFloat_in_assignmentRHS973);
					readExprFloat49=readExprFloat();
					state._fsp--;

					adaptor.addChild(root_0, readExprFloat49.getTree());

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "assignmentRHS"


	public static class methodCallTail_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "methodCallTail"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:351:1: methodCallTail : '(' ( actualParamList )? ')' -> ( actualParamList )? ;
	public final JavaliParser.methodCallTail_return methodCallTail() throws RecognitionException {
		JavaliParser.methodCallTail_return retval = new JavaliParser.methodCallTail_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal50=null;
		Token char_literal52=null;
		ParserRuleReturnScope actualParamList51 =null;

		Object char_literal50_tree=null;
		Object char_literal52_tree=null;
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");
		RewriteRuleSubtreeStream stream_actualParamList=new RewriteRuleSubtreeStream(adaptor,"rule actualParamList");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:352:2: ( '(' ( actualParamList )? ')' -> ( actualParamList )? )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:352:4: '(' ( actualParamList )? ')'
			{
			char_literal50=(Token)match(input,69,FOLLOW_69_in_methodCallTail986);  
			stream_69.add(char_literal50);

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:352:8: ( actualParamList )?
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0==BooleanLiteral||LA18_0==DecimalNumber||LA18_0==FloatNumber||LA18_0==HexNumber||LA18_0==Identifier||LA18_0==65||LA18_0==69||LA18_0==72||LA18_0==74||LA18_0==94||LA18_0==98) ) {
				alt18=1;
			}
			switch (alt18) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:352:8: actualParamList
					{
					pushFollow(FOLLOW_actualParamList_in_methodCallTail988);
					actualParamList51=actualParamList();
					state._fsp--;

					stream_actualParamList.add(actualParamList51.getTree());
					}
					break;

			}

			char_literal52=(Token)match(input,70,FOLLOW_70_in_methodCallTail991);  
			stream_70.add(char_literal52);

			// AST REWRITE
			// elements: actualParamList
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 364:3: -> ( actualParamList )?
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:364:6: ( actualParamList )?
				if ( stream_actualParamList.hasNext() ) {
					adaptor.addChild(root_0, stream_actualParamList.nextTree());
				}
				stream_actualParamList.reset();

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "methodCallTail"


	public static class actualParamList_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "actualParamList"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:367:1: actualParamList : argStart= expr ( ',' expr )* -> ( expr )+ ;
	public final JavaliParser.actualParamList_return actualParamList() throws RecognitionException {
		JavaliParser.actualParamList_return retval = new JavaliParser.actualParamList_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal53=null;
		ParserRuleReturnScope argStart =null;
		ParserRuleReturnScope expr54 =null;

		Object char_literal53_tree=null;
		RewriteRuleTokenStream stream_73=new RewriteRuleTokenStream(adaptor,"token 73");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:368:2: (argStart= expr ( ',' expr )* -> ( expr )+ )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:368:4: argStart= expr ( ',' expr )*
			{
			pushFollow(FOLLOW_expr_in_actualParamList1044);
			argStart=expr();
			state._fsp--;

			stream_expr.add(argStart.getTree());
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:368:18: ( ',' expr )*
			loop19:
			while (true) {
				int alt19=2;
				int LA19_0 = input.LA(1);
				if ( (LA19_0==73) ) {
					alt19=1;
				}

				switch (alt19) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:368:20: ',' expr
					{
					char_literal53=(Token)match(input,73,FOLLOW_73_in_actualParamList1048);  
					stream_73.add(char_literal53);

					pushFollow(FOLLOW_expr_in_actualParamList1050);
					expr54=expr();
					state._fsp--;

					stream_expr.add(expr54.getTree());
					}
					break;

				default :
					break loop19;
				}
			}

			// AST REWRITE
			// elements: expr
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 369:3: -> ( expr )+
			{
				if ( !(stream_expr.hasNext()) ) {
					throw new RewriteEarlyExitException();
				}
				while ( stream_expr.hasNext() ) {
					adaptor.addChild(root_0, stream_expr.nextTree());
				}
				stream_expr.reset();

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "actualParamList"


	public static class ioStmt_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ioStmt"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:372:1: ioStmt : (mth= 'write' '(' expr ')' ';' -> ^( BuiltInWrite[$mth, \"BuiltInWrite\"] expr ) |mth= 'writef' '(' expr ')' ';' -> ^( BuiltInWriteFloat[$mth, \"BuiltInWriteFloat\"] expr ) |mth= 'writeln' '(' ')' ';' -> ^( BuiltInWriteln[$mth, \"BuiltInWriteln\"] ) );
	public final JavaliParser.ioStmt_return ioStmt() throws RecognitionException {
		JavaliParser.ioStmt_return retval = new JavaliParser.ioStmt_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token mth=null;
		Token char_literal55=null;
		Token char_literal57=null;
		Token char_literal58=null;
		Token char_literal59=null;
		Token char_literal61=null;
		Token char_literal62=null;
		Token char_literal63=null;
		Token char_literal64=null;
		Token char_literal65=null;
		ParserRuleReturnScope expr56 =null;
		ParserRuleReturnScope expr60 =null;

		Object mth_tree=null;
		Object char_literal55_tree=null;
		Object char_literal57_tree=null;
		Object char_literal58_tree=null;
		Object char_literal59_tree=null;
		Object char_literal61_tree=null;
		Object char_literal62_tree=null;
		Object char_literal63_tree=null;
		Object char_literal64_tree=null;
		Object char_literal65_tree=null;
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_77=new RewriteRuleTokenStream(adaptor,"token 77");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");
		RewriteRuleTokenStream stream_103=new RewriteRuleTokenStream(adaptor,"token 103");
		RewriteRuleTokenStream stream_102=new RewriteRuleTokenStream(adaptor,"token 102");
		RewriteRuleTokenStream stream_101=new RewriteRuleTokenStream(adaptor,"token 101");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:373:2: (mth= 'write' '(' expr ')' ';' -> ^( BuiltInWrite[$mth, \"BuiltInWrite\"] expr ) |mth= 'writef' '(' expr ')' ';' -> ^( BuiltInWriteFloat[$mth, \"BuiltInWriteFloat\"] expr ) |mth= 'writeln' '(' ')' ';' -> ^( BuiltInWriteln[$mth, \"BuiltInWriteln\"] ) )
			int alt20=3;
			switch ( input.LA(1) ) {
			case 101:
				{
				alt20=1;
				}
				break;
			case 102:
				{
				alt20=2;
				}
				break;
			case 103:
				{
				alt20=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 20, 0, input);
				throw nvae;
			}
			switch (alt20) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:373:4: mth= 'write' '(' expr ')' ';'
					{
					mth=(Token)match(input,101,FOLLOW_101_in_ioStmt1074);  
					stream_101.add(mth);

					char_literal55=(Token)match(input,69,FOLLOW_69_in_ioStmt1076);  
					stream_69.add(char_literal55);

					pushFollow(FOLLOW_expr_in_ioStmt1078);
					expr56=expr();
					state._fsp--;

					stream_expr.add(expr56.getTree());
					char_literal57=(Token)match(input,70,FOLLOW_70_in_ioStmt1080);  
					stream_70.add(char_literal57);

					char_literal58=(Token)match(input,77,FOLLOW_77_in_ioStmt1082);  
					stream_77.add(char_literal58);

					// AST REWRITE
					// elements: expr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 374:5: -> ^( BuiltInWrite[$mth, \"BuiltInWrite\"] expr )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:374:8: ^( BuiltInWrite[$mth, \"BuiltInWrite\"] expr )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BuiltInWrite, mth, "BuiltInWrite"), root_1);
						adaptor.addChild(root_1, stream_expr.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:375:6: mth= 'writef' '(' expr ')' ';'
					{
					mth=(Token)match(input,102,FOLLOW_102_in_ioStmt1106);  
					stream_102.add(mth);

					char_literal59=(Token)match(input,69,FOLLOW_69_in_ioStmt1108);  
					stream_69.add(char_literal59);

					pushFollow(FOLLOW_expr_in_ioStmt1110);
					expr60=expr();
					state._fsp--;

					stream_expr.add(expr60.getTree());
					char_literal61=(Token)match(input,70,FOLLOW_70_in_ioStmt1112);  
					stream_70.add(char_literal61);

					char_literal62=(Token)match(input,77,FOLLOW_77_in_ioStmt1114);  
					stream_77.add(char_literal62);

					// AST REWRITE
					// elements: expr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 376:7: -> ^( BuiltInWriteFloat[$mth, \"BuiltInWriteFloat\"] expr )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:376:10: ^( BuiltInWriteFloat[$mth, \"BuiltInWriteFloat\"] expr )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BuiltInWriteFloat, mth, "BuiltInWriteFloat"), root_1);
						adaptor.addChild(root_1, stream_expr.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:377:6: mth= 'writeln' '(' ')' ';'
					{
					mth=(Token)match(input,103,FOLLOW_103_in_ioStmt1140);  
					stream_103.add(mth);

					char_literal63=(Token)match(input,69,FOLLOW_69_in_ioStmt1142);  
					stream_69.add(char_literal63);

					char_literal64=(Token)match(input,70,FOLLOW_70_in_ioStmt1144);  
					stream_70.add(char_literal64);

					char_literal65=(Token)match(input,77,FOLLOW_77_in_ioStmt1146);  
					stream_77.add(char_literal65);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 378:5: -> ^( BuiltInWriteln[$mth, \"BuiltInWriteln\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:378:8: ^( BuiltInWriteln[$mth, \"BuiltInWriteln\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BuiltInWriteln, mth, "BuiltInWriteln"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ioStmt"


	public static class ifStmt_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "ifStmt"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:381:1: ifStmt : ifStart= 'if' '(' expr ')' then= stmtBlock ( -> ^( IfElse[$ifStart, \"IfElse\"] expr $then ^( Nop[$then.start, \"Nop\"] ) ) | 'else' otherwise= stmtBlock -> ^( IfElse[$ifStart, \"IfElse\"] expr $then $otherwise) ) ;
	public final JavaliParser.ifStmt_return ifStmt() throws RecognitionException {
		JavaliParser.ifStmt_return retval = new JavaliParser.ifStmt_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token ifStart=null;
		Token char_literal66=null;
		Token char_literal68=null;
		Token string_literal69=null;
		ParserRuleReturnScope then =null;
		ParserRuleReturnScope otherwise =null;
		ParserRuleReturnScope expr67 =null;

		Object ifStart_tree=null;
		Object char_literal66_tree=null;
		Object char_literal68_tree=null;
		Object string_literal69_tree=null;
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_91=new RewriteRuleTokenStream(adaptor,"token 91");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");
		RewriteRuleTokenStream stream_88=new RewriteRuleTokenStream(adaptor,"token 88");
		RewriteRuleSubtreeStream stream_stmtBlock=new RewriteRuleSubtreeStream(adaptor,"rule stmtBlock");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:382:2: (ifStart= 'if' '(' expr ')' then= stmtBlock ( -> ^( IfElse[$ifStart, \"IfElse\"] expr $then ^( Nop[$then.start, \"Nop\"] ) ) | 'else' otherwise= stmtBlock -> ^( IfElse[$ifStart, \"IfElse\"] expr $then $otherwise) ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:382:4: ifStart= 'if' '(' expr ')' then= stmtBlock ( -> ^( IfElse[$ifStart, \"IfElse\"] expr $then ^( Nop[$then.start, \"Nop\"] ) ) | 'else' otherwise= stmtBlock -> ^( IfElse[$ifStart, \"IfElse\"] expr $then $otherwise) )
			{
			ifStart=(Token)match(input,91,FOLLOW_91_in_ifStmt1177);  
			stream_91.add(ifStart);

			char_literal66=(Token)match(input,69,FOLLOW_69_in_ifStmt1179);  
			stream_69.add(char_literal66);

			pushFollow(FOLLOW_expr_in_ifStmt1181);
			expr67=expr();
			state._fsp--;

			stream_expr.add(expr67.getTree());
			char_literal68=(Token)match(input,70,FOLLOW_70_in_ifStmt1183);  
			stream_70.add(char_literal68);

			pushFollow(FOLLOW_stmtBlock_in_ifStmt1187);
			then=stmtBlock();
			state._fsp--;

			stream_stmtBlock.add(then.getTree());
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:383:3: ( -> ^( IfElse[$ifStart, \"IfElse\"] expr $then ^( Nop[$then.start, \"Nop\"] ) ) | 'else' otherwise= stmtBlock -> ^( IfElse[$ifStart, \"IfElse\"] expr $then $otherwise) )
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0==Identifier||LA21_0==91||(LA21_0 >= 97 && LA21_0 <= 98)||(LA21_0 >= 100 && LA21_0 <= 103)||LA21_0==106) ) {
				alt21=1;
			}
			else if ( (LA21_0==88) ) {
				alt21=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}

			switch (alt21) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:384:4: 
					{
					// AST REWRITE
					// elements: expr, then
					// token labels: 
					// rule labels: retval, then
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
					RewriteRuleSubtreeStream stream_then=new RewriteRuleSubtreeStream(adaptor,"rule then",then!=null?then.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 384:4: -> ^( IfElse[$ifStart, \"IfElse\"] expr $then ^( Nop[$then.start, \"Nop\"] ) )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:384:7: ^( IfElse[$ifStart, \"IfElse\"] expr $then ^( Nop[$then.start, \"Nop\"] ) )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(IfElse, ifStart, "IfElse"), root_1);
						adaptor.addChild(root_1, stream_expr.nextTree());
						adaptor.addChild(root_1, stream_then.nextTree());
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:384:48: ^( Nop[$then.start, \"Nop\"] )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(Nop, (then!=null?(then.start):null), "Nop"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:385:5: 'else' otherwise= stmtBlock
					{
					string_literal69=(Token)match(input,88,FOLLOW_88_in_ifStmt1222);  
					stream_88.add(string_literal69);

					pushFollow(FOLLOW_stmtBlock_in_ifStmt1226);
					otherwise=stmtBlock();
					state._fsp--;

					stream_stmtBlock.add(otherwise.getTree());
					// AST REWRITE
					// elements: then, expr, otherwise
					// token labels: 
					// rule labels: retval, then, otherwise
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
					RewriteRuleSubtreeStream stream_then=new RewriteRuleSubtreeStream(adaptor,"rule then",then!=null?then.getTree():null);
					RewriteRuleSubtreeStream stream_otherwise=new RewriteRuleSubtreeStream(adaptor,"rule otherwise",otherwise!=null?otherwise.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 386:4: -> ^( IfElse[$ifStart, \"IfElse\"] expr $then $otherwise)
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:386:7: ^( IfElse[$ifStart, \"IfElse\"] expr $then $otherwise)
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(IfElse, ifStart, "IfElse"), root_1);
						adaptor.addChild(root_1, stream_expr.nextTree());
						adaptor.addChild(root_1, stream_then.nextTree());
						adaptor.addChild(root_1, stream_otherwise.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ifStmt"


	public static class whileStmt_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "whileStmt"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:390:1: whileStmt : whileStart= 'while' '(' expr ')' stmtBlock -> ^( WhileLoop[$whileStart, \"WhileLoop\"] expr stmtBlock ) ;
	public final JavaliParser.whileStmt_return whileStmt() throws RecognitionException {
		JavaliParser.whileStmt_return retval = new JavaliParser.whileStmt_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token whileStart=null;
		Token char_literal70=null;
		Token char_literal72=null;
		ParserRuleReturnScope expr71 =null;
		ParserRuleReturnScope stmtBlock73 =null;

		Object whileStart_tree=null;
		Object char_literal70_tree=null;
		Object char_literal72_tree=null;
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");
		RewriteRuleTokenStream stream_100=new RewriteRuleTokenStream(adaptor,"token 100");
		RewriteRuleSubtreeStream stream_stmtBlock=new RewriteRuleSubtreeStream(adaptor,"rule stmtBlock");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:391:2: (whileStart= 'while' '(' expr ')' stmtBlock -> ^( WhileLoop[$whileStart, \"WhileLoop\"] expr stmtBlock ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:391:4: whileStart= 'while' '(' expr ')' stmtBlock
			{
			whileStart=(Token)match(input,100,FOLLOW_100_in_whileStmt1263);  
			stream_100.add(whileStart);

			char_literal70=(Token)match(input,69,FOLLOW_69_in_whileStmt1265);  
			stream_69.add(char_literal70);

			pushFollow(FOLLOW_expr_in_whileStmt1267);
			expr71=expr();
			state._fsp--;

			stream_expr.add(expr71.getTree());
			char_literal72=(Token)match(input,70,FOLLOW_70_in_whileStmt1269);  
			stream_70.add(char_literal72);

			pushFollow(FOLLOW_stmtBlock_in_whileStmt1271);
			stmtBlock73=stmtBlock();
			state._fsp--;

			stream_stmtBlock.add(stmtBlock73.getTree());
			// AST REWRITE
			// elements: expr, stmtBlock
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 392:3: -> ^( WhileLoop[$whileStart, \"WhileLoop\"] expr stmtBlock )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:392:6: ^( WhileLoop[$whileStart, \"WhileLoop\"] expr stmtBlock )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(WhileLoop, whileStart, "WhileLoop"), root_1);
				adaptor.addChild(root_1, stream_expr.nextTree());
				adaptor.addChild(root_1, stream_stmtBlock.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "whileStmt"


	public static class returnStmt_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "returnStmt"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:395:1: returnStmt : 'return' ( expr )? ';' -> ^( ReturnStmt ( expr )? ) ;
	public final JavaliParser.returnStmt_return returnStmt() throws RecognitionException {
		JavaliParser.returnStmt_return retval = new JavaliParser.returnStmt_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal74=null;
		Token char_literal76=null;
		ParserRuleReturnScope expr75 =null;

		Object string_literal74_tree=null;
		Object char_literal76_tree=null;
		RewriteRuleTokenStream stream_97=new RewriteRuleTokenStream(adaptor,"token 97");
		RewriteRuleTokenStream stream_77=new RewriteRuleTokenStream(adaptor,"token 77");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:396:2: ( 'return' ( expr )? ';' -> ^( ReturnStmt ( expr )? ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:396:5: 'return' ( expr )? ';'
			{
			string_literal74=(Token)match(input,97,FOLLOW_97_in_returnStmt1298);  
			stream_97.add(string_literal74);

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:396:14: ( expr )?
			int alt22=2;
			int LA22_0 = input.LA(1);
			if ( (LA22_0==BooleanLiteral||LA22_0==DecimalNumber||LA22_0==FloatNumber||LA22_0==HexNumber||LA22_0==Identifier||LA22_0==65||LA22_0==69||LA22_0==72||LA22_0==74||LA22_0==94||LA22_0==98) ) {
				alt22=1;
			}
			switch (alt22) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:396:14: expr
					{
					pushFollow(FOLLOW_expr_in_returnStmt1300);
					expr75=expr();
					state._fsp--;

					stream_expr.add(expr75.getTree());
					}
					break;

			}

			char_literal76=(Token)match(input,77,FOLLOW_77_in_returnStmt1303);  
			stream_77.add(char_literal76);

			// AST REWRITE
			// elements: expr
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 397:3: -> ^( ReturnStmt ( expr )? )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:397:6: ^( ReturnStmt ( expr )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ReturnStmt, "ReturnStmt"), root_1);
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:397:20: ( expr )?
				if ( stream_expr.hasNext() ) {
					adaptor.addChild(root_1, stream_expr.nextTree());
				}
				stream_expr.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "returnStmt"


	public static class stmtBlock_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "stmtBlock"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:400:1: stmtBlock : lb= '{' ( stmtList )? '}' -> ^( Seq[$lb, \"Seq\"] ( stmtList )? ) ;
	public final JavaliParser.stmtBlock_return stmtBlock() throws RecognitionException {
		JavaliParser.stmtBlock_return retval = new JavaliParser.stmtBlock_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token lb=null;
		Token char_literal78=null;
		ParserRuleReturnScope stmtList77 =null;

		Object lb_tree=null;
		Object char_literal78_tree=null;
		RewriteRuleTokenStream stream_106=new RewriteRuleTokenStream(adaptor,"token 106");
		RewriteRuleTokenStream stream_104=new RewriteRuleTokenStream(adaptor,"token 104");
		RewriteRuleSubtreeStream stream_stmtList=new RewriteRuleSubtreeStream(adaptor,"rule stmtList");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:401:2: (lb= '{' ( stmtList )? '}' -> ^( Seq[$lb, \"Seq\"] ( stmtList )? ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:401:4: lb= '{' ( stmtList )? '}'
			{
			lb=(Token)match(input,104,FOLLOW_104_in_stmtBlock1334);  
			stream_104.add(lb);

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:401:11: ( stmtList )?
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( (LA23_0==Identifier||LA23_0==91||(LA23_0 >= 97 && LA23_0 <= 98)||(LA23_0 >= 100 && LA23_0 <= 103)) ) {
				alt23=1;
			}
			switch (alt23) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:401:11: stmtList
					{
					pushFollow(FOLLOW_stmtList_in_stmtBlock1336);
					stmtList77=stmtList();
					state._fsp--;

					stream_stmtList.add(stmtList77.getTree());
					}
					break;

			}

			char_literal78=(Token)match(input,106,FOLLOW_106_in_stmtBlock1339);  
			stream_106.add(char_literal78);

			// AST REWRITE
			// elements: stmtList
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 402:3: -> ^( Seq[$lb, \"Seq\"] ( stmtList )? )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:402:6: ^( Seq[$lb, \"Seq\"] ( stmtList )? )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Seq, lb, "Seq"), root_1);
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:402:25: ( stmtList )?
				if ( stream_stmtList.hasNext() ) {
					adaptor.addChild(root_1, stream_stmtList.nextTree());
				}
				stream_stmtList.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "stmtBlock"


	public static class newExpr_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "newExpr"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:407:1: newExpr : (kw= 'new' Identifier '(' ')' -> ^( NewObject[$kw, \"NewObject\"] Identifier ) |kw= 'new' id= Identifier '[' simpleExpr ']' -> ^( NewArray[$kw, \"NewArray\"] Identifier[$id, $id.text + \"[]\"] simpleExpr ) |kw= 'new' pt= primitiveType '[' simpleExpr ']' -> ^( NewArray[$kw, \"NewArray\"] Identifier[$pt.start, $pt.text + \"[]\"] simpleExpr ) );
	public final JavaliParser.newExpr_return newExpr() throws RecognitionException {
		JavaliParser.newExpr_return retval = new JavaliParser.newExpr_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token kw=null;
		Token id=null;
		Token Identifier79=null;
		Token char_literal80=null;
		Token char_literal81=null;
		Token char_literal82=null;
		Token char_literal84=null;
		Token char_literal85=null;
		Token char_literal87=null;
		ParserRuleReturnScope pt =null;
		ParserRuleReturnScope simpleExpr83 =null;
		ParserRuleReturnScope simpleExpr86 =null;

		Object kw_tree=null;
		Object id_tree=null;
		Object Identifier79_tree=null;
		Object char_literal80_tree=null;
		Object char_literal81_tree=null;
		Object char_literal82_tree=null;
		Object char_literal84_tree=null;
		Object char_literal85_tree=null;
		Object char_literal87_tree=null;
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_93=new RewriteRuleTokenStream(adaptor,"token 93");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");
		RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
		RewriteRuleTokenStream stream_84=new RewriteRuleTokenStream(adaptor,"token 84");
		RewriteRuleTokenStream stream_85=new RewriteRuleTokenStream(adaptor,"token 85");
		RewriteRuleSubtreeStream stream_simpleExpr=new RewriteRuleSubtreeStream(adaptor,"rule simpleExpr");
		RewriteRuleSubtreeStream stream_primitiveType=new RewriteRuleSubtreeStream(adaptor,"rule primitiveType");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:408:2: (kw= 'new' Identifier '(' ')' -> ^( NewObject[$kw, \"NewObject\"] Identifier ) |kw= 'new' id= Identifier '[' simpleExpr ']' -> ^( NewArray[$kw, \"NewArray\"] Identifier[$id, $id.text + \"[]\"] simpleExpr ) |kw= 'new' pt= primitiveType '[' simpleExpr ']' -> ^( NewArray[$kw, \"NewArray\"] Identifier[$pt.start, $pt.text + \"[]\"] simpleExpr ) )
			int alt24=3;
			int LA24_0 = input.LA(1);
			if ( (LA24_0==93) ) {
				int LA24_1 = input.LA(2);
				if ( (LA24_1==Identifier) ) {
					int LA24_2 = input.LA(3);
					if ( (LA24_2==69) ) {
						alt24=1;
					}
					else if ( (LA24_2==84) ) {
						alt24=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 24, 2, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA24_1==86||LA24_1==90||LA24_1==92) ) {
					alt24=3;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 24, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 24, 0, input);
				throw nvae;
			}

			switch (alt24) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:408:4: kw= 'new' Identifier '(' ')'
					{
					kw=(Token)match(input,93,FOLLOW_93_in_newExpr1369);  
					stream_93.add(kw);

					Identifier79=(Token)match(input,Identifier,FOLLOW_Identifier_in_newExpr1371);  
					stream_Identifier.add(Identifier79);

					char_literal80=(Token)match(input,69,FOLLOW_69_in_newExpr1373);  
					stream_69.add(char_literal80);

					char_literal81=(Token)match(input,70,FOLLOW_70_in_newExpr1375);  
					stream_70.add(char_literal81);

					// AST REWRITE
					// elements: Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 409:3: -> ^( NewObject[$kw, \"NewObject\"] Identifier )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:409:6: ^( NewObject[$kw, \"NewObject\"] Identifier )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NewObject, kw, "NewObject"), root_1);
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:410:4: kw= 'new' id= Identifier '[' simpleExpr ']'
					{
					kw=(Token)match(input,93,FOLLOW_93_in_newExpr1395);  
					stream_93.add(kw);

					id=(Token)match(input,Identifier,FOLLOW_Identifier_in_newExpr1399);  
					stream_Identifier.add(id);

					char_literal82=(Token)match(input,84,FOLLOW_84_in_newExpr1401);  
					stream_84.add(char_literal82);

					pushFollow(FOLLOW_simpleExpr_in_newExpr1403);
					simpleExpr83=simpleExpr();
					state._fsp--;

					stream_simpleExpr.add(simpleExpr83.getTree());
					char_literal84=(Token)match(input,85,FOLLOW_85_in_newExpr1405);  
					stream_85.add(char_literal84);

					// AST REWRITE
					// elements: simpleExpr, Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 411:3: -> ^( NewArray[$kw, \"NewArray\"] Identifier[$id, $id.text + \"[]\"] simpleExpr )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:411:6: ^( NewArray[$kw, \"NewArray\"] Identifier[$id, $id.text + \"[]\"] simpleExpr )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NewArray, kw, "NewArray"), root_1);
						adaptor.addChild(root_1, (Object)adaptor.create(Identifier, id, (id!=null?id.getText():null) + "[]"));
						adaptor.addChild(root_1, stream_simpleExpr.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:412:4: kw= 'new' pt= primitiveType '[' simpleExpr ']'
					{
					kw=(Token)match(input,93,FOLLOW_93_in_newExpr1428);  
					stream_93.add(kw);

					pushFollow(FOLLOW_primitiveType_in_newExpr1432);
					pt=primitiveType();
					state._fsp--;

					stream_primitiveType.add(pt.getTree());
					char_literal85=(Token)match(input,84,FOLLOW_84_in_newExpr1434);  
					stream_84.add(char_literal85);

					pushFollow(FOLLOW_simpleExpr_in_newExpr1436);
					simpleExpr86=simpleExpr();
					state._fsp--;

					stream_simpleExpr.add(simpleExpr86.getTree());
					char_literal87=(Token)match(input,85,FOLLOW_85_in_newExpr1438);  
					stream_85.add(char_literal87);

					// AST REWRITE
					// elements: simpleExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 413:3: -> ^( NewArray[$kw, \"NewArray\"] Identifier[$pt.start, $pt.text + \"[]\"] simpleExpr )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:413:6: ^( NewArray[$kw, \"NewArray\"] Identifier[$pt.start, $pt.text + \"[]\"] simpleExpr )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NewArray, kw, "NewArray"), root_1);
						adaptor.addChild(root_1, (Object)adaptor.create(Identifier, (pt!=null?(pt.start):null), (pt!=null?input.toString(pt.start,pt.stop):null) + "[]"));
						adaptor.addChild(root_1, stream_simpleExpr.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "newExpr"


	public static class readExpr_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "readExpr"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:416:1: readExpr : kw= 'read' '(' ')' -> ^( BuiltInRead[$kw, \"BuiltInRead\"] ) ;
	public final JavaliParser.readExpr_return readExpr() throws RecognitionException {
		JavaliParser.readExpr_return retval = new JavaliParser.readExpr_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token kw=null;
		Token char_literal88=null;
		Token char_literal89=null;

		Object kw_tree=null;
		Object char_literal88_tree=null;
		Object char_literal89_tree=null;
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_95=new RewriteRuleTokenStream(adaptor,"token 95");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:417:2: (kw= 'read' '(' ')' -> ^( BuiltInRead[$kw, \"BuiltInRead\"] ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:417:4: kw= 'read' '(' ')'
			{
			kw=(Token)match(input,95,FOLLOW_95_in_readExpr1467);  
			stream_95.add(kw);

			char_literal88=(Token)match(input,69,FOLLOW_69_in_readExpr1469);  
			stream_69.add(char_literal88);

			char_literal89=(Token)match(input,70,FOLLOW_70_in_readExpr1471);  
			stream_70.add(char_literal89);

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 418:3: -> ^( BuiltInRead[$kw, \"BuiltInRead\"] )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:418:6: ^( BuiltInRead[$kw, \"BuiltInRead\"] )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BuiltInRead, kw, "BuiltInRead"), root_1);
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "readExpr"


	public static class readExprFloat_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "readExprFloat"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:421:1: readExprFloat : kw= 'readf' '(' ')' -> ^( BuiltInReadFloat[$kw, \"BuiltInReadFloat\"] ) ;
	public final JavaliParser.readExprFloat_return readExprFloat() throws RecognitionException {
		JavaliParser.readExprFloat_return retval = new JavaliParser.readExprFloat_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token kw=null;
		Token char_literal90=null;
		Token char_literal91=null;

		Object kw_tree=null;
		Object char_literal90_tree=null;
		Object char_literal91_tree=null;
		RewriteRuleTokenStream stream_96=new RewriteRuleTokenStream(adaptor,"token 96");
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:422:4: (kw= 'readf' '(' ')' -> ^( BuiltInReadFloat[$kw, \"BuiltInReadFloat\"] ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:422:6: kw= 'readf' '(' ')'
			{
			kw=(Token)match(input,96,FOLLOW_96_in_readExprFloat1497);  
			stream_96.add(kw);

			char_literal90=(Token)match(input,69,FOLLOW_69_in_readExprFloat1499);  
			stream_69.add(char_literal90);

			char_literal91=(Token)match(input,70,FOLLOW_70_in_readExprFloat1501);  
			stream_70.add(char_literal91);

			// AST REWRITE
			// elements: 
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 423:7: -> ^( BuiltInReadFloat[$kw, \"BuiltInReadFloat\"] )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:423:10: ^( BuiltInReadFloat[$kw, \"BuiltInReadFloat\"] )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BuiltInReadFloat, kw, "BuiltInReadFloat"), root_1);
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "readExprFloat"


	public static class expr_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "expr"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:426:1: expr : leftExpr= simpleExpr ( -> $leftExpr|op= compOp rightExpr= simpleExpr -> ^( BinaryOp[$op.start, \"BinaryOp\"] $leftExpr compOp $rightExpr) ) ;
	public final JavaliParser.expr_return expr() throws RecognitionException {
		JavaliParser.expr_return retval = new JavaliParser.expr_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope leftExpr =null;
		ParserRuleReturnScope op =null;
		ParserRuleReturnScope rightExpr =null;

		RewriteRuleSubtreeStream stream_simpleExpr=new RewriteRuleSubtreeStream(adaptor,"rule simpleExpr");
		RewriteRuleSubtreeStream stream_compOp=new RewriteRuleSubtreeStream(adaptor,"rule compOp");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:427:2: (leftExpr= simpleExpr ( -> $leftExpr|op= compOp rightExpr= simpleExpr -> ^( BinaryOp[$op.start, \"BinaryOp\"] $leftExpr compOp $rightExpr) ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:427:4: leftExpr= simpleExpr ( -> $leftExpr|op= compOp rightExpr= simpleExpr -> ^( BinaryOp[$op.start, \"BinaryOp\"] $leftExpr compOp $rightExpr) )
			{
			pushFollow(FOLLOW_simpleExpr_in_expr1531);
			leftExpr=simpleExpr();
			state._fsp--;

			stream_simpleExpr.add(leftExpr.getTree());
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:428:3: ( -> $leftExpr|op= compOp rightExpr= simpleExpr -> ^( BinaryOp[$op.start, \"BinaryOp\"] $leftExpr compOp $rightExpr) )
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==70||LA25_0==73||LA25_0==77) ) {
				alt25=1;
			}
			else if ( (LA25_0==66||(LA25_0 >= 78 && LA25_0 <= 79)||(LA25_0 >= 81 && LA25_0 <= 83)) ) {
				alt25=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 25, 0, input);
				throw nvae;
			}

			switch (alt25) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:429:4: 
					{
					// AST REWRITE
					// elements: leftExpr
					// token labels: 
					// rule labels: leftExpr, retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_leftExpr=new RewriteRuleSubtreeStream(adaptor,"rule leftExpr",leftExpr!=null?leftExpr.getTree():null);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 429:4: -> $leftExpr
					{
						adaptor.addChild(root_0, stream_leftExpr.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:430:5: op= compOp rightExpr= simpleExpr
					{
					pushFollow(FOLLOW_compOp_in_expr1552);
					op=compOp();
					state._fsp--;

					stream_compOp.add(op.getTree());
					pushFollow(FOLLOW_simpleExpr_in_expr1556);
					rightExpr=simpleExpr();
					state._fsp--;

					stream_simpleExpr.add(rightExpr.getTree());
					// AST REWRITE
					// elements: compOp, leftExpr, rightExpr
					// token labels: 
					// rule labels: leftExpr, retval, rightExpr
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_leftExpr=new RewriteRuleSubtreeStream(adaptor,"rule leftExpr",leftExpr!=null?leftExpr.getTree():null);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
					RewriteRuleSubtreeStream stream_rightExpr=new RewriteRuleSubtreeStream(adaptor,"rule rightExpr",rightExpr!=null?rightExpr.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 431:4: -> ^( BinaryOp[$op.start, \"BinaryOp\"] $leftExpr compOp $rightExpr)
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:431:7: ^( BinaryOp[$op.start, \"BinaryOp\"] $leftExpr compOp $rightExpr)
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BinaryOp, (op!=null?(op.start):null), "BinaryOp"), root_1);
						adaptor.addChild(root_1, stream_leftExpr.nextTree());
						adaptor.addChild(root_1, stream_compOp.nextTree());
						adaptor.addChild(root_1, stream_rightExpr.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expr"


	public static class compOp_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "compOp"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:435:1: compOp : (op= '==' -> ^( B_EQUAL[$op, \"B_EQUAL\"] ) |op= '!=' -> ^( B_NOT_EQUAL[$op, \"B_NOT_EQUAL\"] ) |op= '<' -> ^( B_LESS_THAN[$op, \"B_LESS_THAN\"] ) |op= '<=' -> ^( B_LESS_OR_EQUAL[$op, \"B_LESS_OR_EQUAL\"] ) |op= '>' -> ^( B_GREATER_THAN[$op, \"B_GREATER_THAN\"] ) |op= '>=' -> ^( B_GREATER_OR_EQUAL[$op, \"B_GREATER_OR_EQUAL\"] ) );
	public final JavaliParser.compOp_return compOp() throws RecognitionException {
		JavaliParser.compOp_return retval = new JavaliParser.compOp_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token op=null;

		Object op_tree=null;
		RewriteRuleTokenStream stream_79=new RewriteRuleTokenStream(adaptor,"token 79");
		RewriteRuleTokenStream stream_66=new RewriteRuleTokenStream(adaptor,"token 66");
		RewriteRuleTokenStream stream_78=new RewriteRuleTokenStream(adaptor,"token 78");
		RewriteRuleTokenStream stream_82=new RewriteRuleTokenStream(adaptor,"token 82");
		RewriteRuleTokenStream stream_83=new RewriteRuleTokenStream(adaptor,"token 83");
		RewriteRuleTokenStream stream_81=new RewriteRuleTokenStream(adaptor,"token 81");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:436:2: (op= '==' -> ^( B_EQUAL[$op, \"B_EQUAL\"] ) |op= '!=' -> ^( B_NOT_EQUAL[$op, \"B_NOT_EQUAL\"] ) |op= '<' -> ^( B_LESS_THAN[$op, \"B_LESS_THAN\"] ) |op= '<=' -> ^( B_LESS_OR_EQUAL[$op, \"B_LESS_OR_EQUAL\"] ) |op= '>' -> ^( B_GREATER_THAN[$op, \"B_GREATER_THAN\"] ) |op= '>=' -> ^( B_GREATER_OR_EQUAL[$op, \"B_GREATER_OR_EQUAL\"] ) )
			int alt26=6;
			switch ( input.LA(1) ) {
			case 81:
				{
				alt26=1;
				}
				break;
			case 66:
				{
				alt26=2;
				}
				break;
			case 78:
				{
				alt26=3;
				}
				break;
			case 79:
				{
				alt26=4;
				}
				break;
			case 82:
				{
				alt26=5;
				}
				break;
			case 83:
				{
				alt26=6;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 26, 0, input);
				throw nvae;
			}
			switch (alt26) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:436:4: op= '=='
					{
					op=(Token)match(input,81,FOLLOW_81_in_compOp1593);  
					stream_81.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 437:3: -> ^( B_EQUAL[$op, \"B_EQUAL\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:437:6: ^( B_EQUAL[$op, \"B_EQUAL\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_EQUAL, op, "B_EQUAL"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:438:4: op= '!='
					{
					op=(Token)match(input,66,FOLLOW_66_in_compOp1611);  
					stream_66.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 439:3: -> ^( B_NOT_EQUAL[$op, \"B_NOT_EQUAL\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:439:6: ^( B_NOT_EQUAL[$op, \"B_NOT_EQUAL\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_NOT_EQUAL, op, "B_NOT_EQUAL"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:440:4: op= '<'
					{
					op=(Token)match(input,78,FOLLOW_78_in_compOp1629);  
					stream_78.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 441:3: -> ^( B_LESS_THAN[$op, \"B_LESS_THAN\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:441:6: ^( B_LESS_THAN[$op, \"B_LESS_THAN\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_LESS_THAN, op, "B_LESS_THAN"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 4 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:442:4: op= '<='
					{
					op=(Token)match(input,79,FOLLOW_79_in_compOp1647);  
					stream_79.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 443:3: -> ^( B_LESS_OR_EQUAL[$op, \"B_LESS_OR_EQUAL\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:443:6: ^( B_LESS_OR_EQUAL[$op, \"B_LESS_OR_EQUAL\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_LESS_OR_EQUAL, op, "B_LESS_OR_EQUAL"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 5 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:444:4: op= '>'
					{
					op=(Token)match(input,82,FOLLOW_82_in_compOp1665);  
					stream_82.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 445:3: -> ^( B_GREATER_THAN[$op, \"B_GREATER_THAN\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:445:6: ^( B_GREATER_THAN[$op, \"B_GREATER_THAN\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_GREATER_THAN, op, "B_GREATER_THAN"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 6 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:446:4: op= '>='
					{
					op=(Token)match(input,83,FOLLOW_83_in_compOp1683);  
					stream_83.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 447:3: -> ^( B_GREATER_OR_EQUAL[$op, \"B_GREATER_OR_EQUAL\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:447:6: ^( B_GREATER_OR_EQUAL[$op, \"B_GREATER_OR_EQUAL\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_GREATER_OR_EQUAL, op, "B_GREATER_OR_EQUAL"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "compOp"


	public static class simpleExpr_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simpleExpr"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:450:1: simpleExpr : ( term -> term ) (op= weakOp rightTerm= term -> ^( BinaryOp[$op.start, \"BinaryOp\"] $simpleExpr weakOp $rightTerm) )* ;
	public final JavaliParser.simpleExpr_return simpleExpr() throws RecognitionException {
		JavaliParser.simpleExpr_return retval = new JavaliParser.simpleExpr_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope op =null;
		ParserRuleReturnScope rightTerm =null;
		ParserRuleReturnScope term92 =null;

		RewriteRuleSubtreeStream stream_weakOp=new RewriteRuleSubtreeStream(adaptor,"rule weakOp");
		RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:451:2: ( ( term -> term ) (op= weakOp rightTerm= term -> ^( BinaryOp[$op.start, \"BinaryOp\"] $simpleExpr weakOp $rightTerm) )* )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:452:3: ( term -> term ) (op= weakOp rightTerm= term -> ^( BinaryOp[$op.start, \"BinaryOp\"] $simpleExpr weakOp $rightTerm) )*
			{
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:452:3: ( term -> term )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:452:5: term
			{
			pushFollow(FOLLOW_term_in_simpleExpr1710);
			term92=term();
			state._fsp--;

			stream_term.add(term92.getTree());
			// AST REWRITE
			// elements: term
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 452:10: -> term
			{
				adaptor.addChild(root_0, stream_term.nextTree());
			}


			retval.tree = root_0;

			}

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:453:3: (op= weakOp rightTerm= term -> ^( BinaryOp[$op.start, \"BinaryOp\"] $simpleExpr weakOp $rightTerm) )*
			loop27:
			while (true) {
				int alt27=2;
				int LA27_0 = input.LA(1);
				if ( (LA27_0==72||LA27_0==74||LA27_0==105) ) {
					alt27=1;
				}

				switch (alt27) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:453:5: op= weakOp rightTerm= term
					{
					pushFollow(FOLLOW_weakOp_in_simpleExpr1724);
					op=weakOp();
					state._fsp--;

					stream_weakOp.add(op.getTree());
					pushFollow(FOLLOW_term_in_simpleExpr1728);
					rightTerm=term();
					state._fsp--;

					stream_term.add(rightTerm.getTree());
					// AST REWRITE
					// elements: rightTerm, weakOp, simpleExpr
					// token labels: 
					// rule labels: retval, rightTerm
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
					RewriteRuleSubtreeStream stream_rightTerm=new RewriteRuleSubtreeStream(adaptor,"rule rightTerm",rightTerm!=null?rightTerm.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 454:4: -> ^( BinaryOp[$op.start, \"BinaryOp\"] $simpleExpr weakOp $rightTerm)
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:454:7: ^( BinaryOp[$op.start, \"BinaryOp\"] $simpleExpr weakOp $rightTerm)
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BinaryOp, (op!=null?(op.start):null), "BinaryOp"), root_1);
						adaptor.addChild(root_1, stream_retval.nextTree());
						adaptor.addChild(root_1, stream_weakOp.nextTree());
						adaptor.addChild(root_1, stream_rightTerm.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

				default :
					break loop27;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simpleExpr"


	public static class weakOp_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "weakOp"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:458:1: weakOp : (op= '+' -> ^( B_PLUS[$op, \"B_PLUS\"] ) |op= '-' -> ^( B_MINUS[$op, \"B_MINUS\"] ) |op= '||' -> ^( B_OR[$op, \"B_OR\"] ) );
	public final JavaliParser.weakOp_return weakOp() throws RecognitionException {
		JavaliParser.weakOp_return retval = new JavaliParser.weakOp_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token op=null;

		Object op_tree=null;
		RewriteRuleTokenStream stream_105=new RewriteRuleTokenStream(adaptor,"token 105");
		RewriteRuleTokenStream stream_72=new RewriteRuleTokenStream(adaptor,"token 72");
		RewriteRuleTokenStream stream_74=new RewriteRuleTokenStream(adaptor,"token 74");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:459:2: (op= '+' -> ^( B_PLUS[$op, \"B_PLUS\"] ) |op= '-' -> ^( B_MINUS[$op, \"B_MINUS\"] ) |op= '||' -> ^( B_OR[$op, \"B_OR\"] ) )
			int alt28=3;
			switch ( input.LA(1) ) {
			case 72:
				{
				alt28=1;
				}
				break;
			case 74:
				{
				alt28=2;
				}
				break;
			case 105:
				{
				alt28=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 28, 0, input);
				throw nvae;
			}
			switch (alt28) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:459:4: op= '+'
					{
					op=(Token)match(input,72,FOLLOW_72_in_weakOp1766);  
					stream_72.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 460:3: -> ^( B_PLUS[$op, \"B_PLUS\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:460:6: ^( B_PLUS[$op, \"B_PLUS\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_PLUS, op, "B_PLUS"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:461:4: op= '-'
					{
					op=(Token)match(input,74,FOLLOW_74_in_weakOp1784);  
					stream_74.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 462:3: -> ^( B_MINUS[$op, \"B_MINUS\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:462:6: ^( B_MINUS[$op, \"B_MINUS\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_MINUS, op, "B_MINUS"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:463:4: op= '||'
					{
					op=(Token)match(input,105,FOLLOW_105_in_weakOp1802);  
					stream_105.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 464:3: -> ^( B_OR[$op, \"B_OR\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:464:6: ^( B_OR[$op, \"B_OR\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_OR, op, "B_OR"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "weakOp"


	public static class term_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "term"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:467:1: term : ( factor -> factor ) (op= strongOp rightFactor= factor -> ^( BinaryOp[$op.start, \"BinaryOp\"] $term strongOp $rightFactor) )* ;
	public final JavaliParser.term_return term() throws RecognitionException {
		JavaliParser.term_return retval = new JavaliParser.term_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope op =null;
		ParserRuleReturnScope rightFactor =null;
		ParserRuleReturnScope factor93 =null;

		RewriteRuleSubtreeStream stream_strongOp=new RewriteRuleSubtreeStream(adaptor,"rule strongOp");
		RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:468:2: ( ( factor -> factor ) (op= strongOp rightFactor= factor -> ^( BinaryOp[$op.start, \"BinaryOp\"] $term strongOp $rightFactor) )* )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:469:3: ( factor -> factor ) (op= strongOp rightFactor= factor -> ^( BinaryOp[$op.start, \"BinaryOp\"] $term strongOp $rightFactor) )*
			{
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:469:3: ( factor -> factor )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:469:5: factor
			{
			pushFollow(FOLLOW_factor_in_term1829);
			factor93=factor();
			state._fsp--;

			stream_factor.add(factor93.getTree());
			// AST REWRITE
			// elements: factor
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 469:12: -> factor
			{
				adaptor.addChild(root_0, stream_factor.nextTree());
			}


			retval.tree = root_0;

			}

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:470:3: (op= strongOp rightFactor= factor -> ^( BinaryOp[$op.start, \"BinaryOp\"] $term strongOp $rightFactor) )*
			loop29:
			while (true) {
				int alt29=2;
				int LA29_0 = input.LA(1);
				if ( ((LA29_0 >= 67 && LA29_0 <= 68)||LA29_0==71||LA29_0==76) ) {
					alt29=1;
				}

				switch (alt29) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:470:5: op= strongOp rightFactor= factor
					{
					pushFollow(FOLLOW_strongOp_in_term1843);
					op=strongOp();
					state._fsp--;

					stream_strongOp.add(op.getTree());
					pushFollow(FOLLOW_factor_in_term1847);
					rightFactor=factor();
					state._fsp--;

					stream_factor.add(rightFactor.getTree());
					// AST REWRITE
					// elements: rightFactor, strongOp, term
					// token labels: 
					// rule labels: retval, rightFactor
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);
					RewriteRuleSubtreeStream stream_rightFactor=new RewriteRuleSubtreeStream(adaptor,"rule rightFactor",rightFactor!=null?rightFactor.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 471:4: -> ^( BinaryOp[$op.start, \"BinaryOp\"] $term strongOp $rightFactor)
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:471:7: ^( BinaryOp[$op.start, \"BinaryOp\"] $term strongOp $rightFactor)
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BinaryOp, (op!=null?(op.start):null), "BinaryOp"), root_1);
						adaptor.addChild(root_1, stream_retval.nextTree());
						adaptor.addChild(root_1, stream_strongOp.nextTree());
						adaptor.addChild(root_1, stream_rightFactor.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

				default :
					break loop29;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "term"


	public static class strongOp_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "strongOp"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:475:1: strongOp : (op= '*' -> ^( B_TIMES[$op, \"B_TIMES\"] ) |op= '/' -> ^( B_DIV[$op, \"B_DIV\"] ) |op= '%' -> ^( B_MOD[$op, \"B_MOD\"] ) |op= '&&' -> ^( B_AND[$op, \"B_AND\"] ) );
	public final JavaliParser.strongOp_return strongOp() throws RecognitionException {
		JavaliParser.strongOp_return retval = new JavaliParser.strongOp_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token op=null;

		Object op_tree=null;
		RewriteRuleTokenStream stream_67=new RewriteRuleTokenStream(adaptor,"token 67");
		RewriteRuleTokenStream stream_68=new RewriteRuleTokenStream(adaptor,"token 68");
		RewriteRuleTokenStream stream_71=new RewriteRuleTokenStream(adaptor,"token 71");
		RewriteRuleTokenStream stream_76=new RewriteRuleTokenStream(adaptor,"token 76");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:476:2: (op= '*' -> ^( B_TIMES[$op, \"B_TIMES\"] ) |op= '/' -> ^( B_DIV[$op, \"B_DIV\"] ) |op= '%' -> ^( B_MOD[$op, \"B_MOD\"] ) |op= '&&' -> ^( B_AND[$op, \"B_AND\"] ) )
			int alt30=4;
			switch ( input.LA(1) ) {
			case 71:
				{
				alt30=1;
				}
				break;
			case 76:
				{
				alt30=2;
				}
				break;
			case 67:
				{
				alt30=3;
				}
				break;
			case 68:
				{
				alt30=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 30, 0, input);
				throw nvae;
			}
			switch (alt30) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:476:4: op= '*'
					{
					op=(Token)match(input,71,FOLLOW_71_in_strongOp1885);  
					stream_71.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 477:3: -> ^( B_TIMES[$op, \"B_TIMES\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:477:6: ^( B_TIMES[$op, \"B_TIMES\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_TIMES, op, "B_TIMES"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:478:4: op= '/'
					{
					op=(Token)match(input,76,FOLLOW_76_in_strongOp1903);  
					stream_76.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 479:3: -> ^( B_DIV[$op, \"B_DIV\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:479:6: ^( B_DIV[$op, \"B_DIV\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_DIV, op, "B_DIV"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:480:4: op= '%'
					{
					op=(Token)match(input,67,FOLLOW_67_in_strongOp1921);  
					stream_67.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 481:3: -> ^( B_MOD[$op, \"B_MOD\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:481:6: ^( B_MOD[$op, \"B_MOD\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_MOD, op, "B_MOD"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 4 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:482:4: op= '&&'
					{
					op=(Token)match(input,68,FOLLOW_68_in_strongOp1939);  
					stream_68.add(op);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 483:3: -> ^( B_AND[$op, \"B_AND\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:483:6: ^( B_AND[$op, \"B_AND\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(B_AND, op, "B_AND"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "strongOp"


	public static class factor_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "factor"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:486:1: factor : (op= '+' noSignFactor -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_PLUS[$op, \"U_PLUS\"] ) noSignFactor ) |op= '-' noSignFactor -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_MINUS[$op, \"U_MINUS\"] ) noSignFactor ) | noSignFactor );
	public final JavaliParser.factor_return factor() throws RecognitionException {
		JavaliParser.factor_return retval = new JavaliParser.factor_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token op=null;
		ParserRuleReturnScope noSignFactor94 =null;
		ParserRuleReturnScope noSignFactor95 =null;
		ParserRuleReturnScope noSignFactor96 =null;

		Object op_tree=null;
		RewriteRuleTokenStream stream_72=new RewriteRuleTokenStream(adaptor,"token 72");
		RewriteRuleTokenStream stream_74=new RewriteRuleTokenStream(adaptor,"token 74");
		RewriteRuleSubtreeStream stream_noSignFactor=new RewriteRuleSubtreeStream(adaptor,"rule noSignFactor");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:487:2: (op= '+' noSignFactor -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_PLUS[$op, \"U_PLUS\"] ) noSignFactor ) |op= '-' noSignFactor -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_MINUS[$op, \"U_MINUS\"] ) noSignFactor ) | noSignFactor )
			int alt31=3;
			switch ( input.LA(1) ) {
			case 72:
				{
				alt31=1;
				}
				break;
			case 74:
				{
				alt31=2;
				}
				break;
			case BooleanLiteral:
			case DecimalNumber:
			case FloatNumber:
			case HexNumber:
			case Identifier:
			case 65:
			case 69:
			case 94:
			case 98:
				{
				alt31=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 31, 0, input);
				throw nvae;
			}
			switch (alt31) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:487:4: op= '+' noSignFactor
					{
					op=(Token)match(input,72,FOLLOW_72_in_factor1963);  
					stream_72.add(op);

					pushFollow(FOLLOW_noSignFactor_in_factor1965);
					noSignFactor94=noSignFactor();
					state._fsp--;

					stream_noSignFactor.add(noSignFactor94.getTree());
					// AST REWRITE
					// elements: noSignFactor
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 488:3: -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_PLUS[$op, \"U_PLUS\"] ) noSignFactor )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:488:6: ^( UnaryOp[$op, \"UnaryOp\"] ^( U_PLUS[$op, \"U_PLUS\"] ) noSignFactor )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(UnaryOp, op, "UnaryOp"), root_1);
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:488:33: ^( U_PLUS[$op, \"U_PLUS\"] )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(U_PLUS, op, "U_PLUS"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_1, stream_noSignFactor.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:489:4: op= '-' noSignFactor
					{
					op=(Token)match(input,74,FOLLOW_74_in_factor1992);  
					stream_74.add(op);

					pushFollow(FOLLOW_noSignFactor_in_factor1994);
					noSignFactor95=noSignFactor();
					state._fsp--;

					stream_noSignFactor.add(noSignFactor95.getTree());
					// AST REWRITE
					// elements: noSignFactor
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 490:3: -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_MINUS[$op, \"U_MINUS\"] ) noSignFactor )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:490:6: ^( UnaryOp[$op, \"UnaryOp\"] ^( U_MINUS[$op, \"U_MINUS\"] ) noSignFactor )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(UnaryOp, op, "UnaryOp"), root_1);
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:490:33: ^( U_MINUS[$op, \"U_MINUS\"] )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(U_MINUS, op, "U_MINUS"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_1, stream_noSignFactor.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:491:4: noSignFactor
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_noSignFactor_in_factor2019);
					noSignFactor96=noSignFactor();
					state._fsp--;

					adaptor.addChild(root_0, noSignFactor96.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "factor"


	public static class noSignFactor_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "noSignFactor"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:494:1: noSignFactor : (op= '!' factor -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_BOOL_NOT[$op, \"U_BOOL_NOT\"] ) factor ) |val= DecimalNumber -> ^( DecimalIntConst[$val] ) |val= HexNumber -> ^( HexIntConst[$val] ) |val= FloatNumber -> ^( FloatConst[$val] ) |val= BooleanLiteral -> ^( BooleanConst[$val] ) |kw= 'null' -> ^( NullConst[$kw, \"NullConst\"] ) |target= identAccess -> identAccess | '(' expr ')' -> expr |lp= '(' referenceType ')' noSignFactor -> ^( Cast[$lp, \"Cast\"] noSignFactor referenceType ) );
	public final JavaliParser.noSignFactor_return noSignFactor() throws RecognitionException {
		JavaliParser.noSignFactor_return retval = new JavaliParser.noSignFactor_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token op=null;
		Token val=null;
		Token kw=null;
		Token lp=null;
		Token char_literal98=null;
		Token char_literal100=null;
		Token char_literal102=null;
		ParserRuleReturnScope target =null;
		ParserRuleReturnScope factor97 =null;
		ParserRuleReturnScope expr99 =null;
		ParserRuleReturnScope referenceType101 =null;
		ParserRuleReturnScope noSignFactor103 =null;

		Object op_tree=null;
		Object val_tree=null;
		Object kw_tree=null;
		Object lp_tree=null;
		Object char_literal98_tree=null;
		Object char_literal100_tree=null;
		Object char_literal102_tree=null;
		RewriteRuleTokenStream stream_HexNumber=new RewriteRuleTokenStream(adaptor,"token HexNumber");
		RewriteRuleTokenStream stream_69=new RewriteRuleTokenStream(adaptor,"token 69");
		RewriteRuleTokenStream stream_94=new RewriteRuleTokenStream(adaptor,"token 94");
		RewriteRuleTokenStream stream_DecimalNumber=new RewriteRuleTokenStream(adaptor,"token DecimalNumber");
		RewriteRuleTokenStream stream_70=new RewriteRuleTokenStream(adaptor,"token 70");
		RewriteRuleTokenStream stream_FloatNumber=new RewriteRuleTokenStream(adaptor,"token FloatNumber");
		RewriteRuleTokenStream stream_65=new RewriteRuleTokenStream(adaptor,"token 65");
		RewriteRuleTokenStream stream_BooleanLiteral=new RewriteRuleTokenStream(adaptor,"token BooleanLiteral");
		RewriteRuleSubtreeStream stream_noSignFactor=new RewriteRuleSubtreeStream(adaptor,"rule noSignFactor");
		RewriteRuleSubtreeStream stream_referenceType=new RewriteRuleSubtreeStream(adaptor,"rule referenceType");
		RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
		RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
		RewriteRuleSubtreeStream stream_identAccess=new RewriteRuleSubtreeStream(adaptor,"rule identAccess");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:495:2: (op= '!' factor -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_BOOL_NOT[$op, \"U_BOOL_NOT\"] ) factor ) |val= DecimalNumber -> ^( DecimalIntConst[$val] ) |val= HexNumber -> ^( HexIntConst[$val] ) |val= FloatNumber -> ^( FloatConst[$val] ) |val= BooleanLiteral -> ^( BooleanConst[$val] ) |kw= 'null' -> ^( NullConst[$kw, \"NullConst\"] ) |target= identAccess -> identAccess | '(' expr ')' -> expr |lp= '(' referenceType ')' noSignFactor -> ^( Cast[$lp, \"Cast\"] noSignFactor referenceType ) )
			int alt32=9;
			switch ( input.LA(1) ) {
			case 65:
				{
				alt32=1;
				}
				break;
			case DecimalNumber:
				{
				alt32=2;
				}
				break;
			case HexNumber:
				{
				alt32=3;
				}
				break;
			case FloatNumber:
				{
				alt32=4;
				}
				break;
			case BooleanLiteral:
				{
				alt32=5;
				}
				break;
			case 94:
				{
				alt32=6;
				}
				break;
			case Identifier:
			case 98:
				{
				alt32=7;
				}
				break;
			case 69:
				{
				switch ( input.LA(2) ) {
				case BooleanLiteral:
				case DecimalNumber:
				case FloatNumber:
				case HexNumber:
				case 65:
				case 69:
				case 72:
				case 74:
				case 94:
				case 98:
					{
					alt32=8;
					}
					break;
				case Identifier:
					{
					switch ( input.LA(3) ) {
					case 84:
						{
						int LA32_12 = input.LA(4);
						if ( (LA32_12==85) ) {
							alt32=9;
						}
						else if ( (LA32_12==BooleanLiteral||LA32_12==DecimalNumber||LA32_12==FloatNumber||LA32_12==HexNumber||LA32_12==Identifier||LA32_12==65||LA32_12==69||LA32_12==72||LA32_12==74||LA32_12==94||LA32_12==98) ) {
							alt32=8;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 32, 12, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

						}
						break;
					case 66:
					case 67:
					case 68:
					case 69:
					case 71:
					case 72:
					case 74:
					case 75:
					case 76:
					case 78:
					case 79:
					case 81:
					case 82:
					case 83:
					case 105:
						{
						alt32=8;
						}
						break;
					case 70:
						{
						int LA32_13 = input.LA(4);
						if ( ((LA32_13 >= 66 && LA32_13 <= 68)||(LA32_13 >= 70 && LA32_13 <= 74)||(LA32_13 >= 76 && LA32_13 <= 79)||(LA32_13 >= 81 && LA32_13 <= 83)||LA32_13==85||LA32_13==105) ) {
							alt32=8;
						}
						else if ( (LA32_13==BooleanLiteral||LA32_13==DecimalNumber||LA32_13==FloatNumber||LA32_13==HexNumber||LA32_13==Identifier||LA32_13==65||LA32_13==69||LA32_13==94||LA32_13==98) ) {
							alt32=9;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 32, 13, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

						}
						break;
					default:
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 32, 10, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case 86:
				case 90:
				case 92:
					{
					alt32=9;
					}
					break;
				default:
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 32, 8, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 32, 0, input);
				throw nvae;
			}
			switch (alt32) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:495:4: op= '!' factor
					{
					op=(Token)match(input,65,FOLLOW_65_in_noSignFactor2032);  
					stream_65.add(op);

					pushFollow(FOLLOW_factor_in_noSignFactor2034);
					factor97=factor();
					state._fsp--;

					stream_factor.add(factor97.getTree());
					// AST REWRITE
					// elements: factor
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 496:3: -> ^( UnaryOp[$op, \"UnaryOp\"] ^( U_BOOL_NOT[$op, \"U_BOOL_NOT\"] ) factor )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:496:6: ^( UnaryOp[$op, \"UnaryOp\"] ^( U_BOOL_NOT[$op, \"U_BOOL_NOT\"] ) factor )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(UnaryOp, op, "UnaryOp"), root_1);
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:496:33: ^( U_BOOL_NOT[$op, \"U_BOOL_NOT\"] )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(U_BOOL_NOT, op, "U_BOOL_NOT"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_1, stream_factor.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:497:4: val= DecimalNumber
					{
					val=(Token)match(input,DecimalNumber,FOLLOW_DecimalNumber_in_noSignFactor2061);  
					stream_DecimalNumber.add(val);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 498:3: -> ^( DecimalIntConst[$val] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:498:6: ^( DecimalIntConst[$val] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(DecimalIntConst, val), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:499:4: val= HexNumber
					{
					val=(Token)match(input,HexNumber,FOLLOW_HexNumber_in_noSignFactor2080);  
					stream_HexNumber.add(val);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 500:3: -> ^( HexIntConst[$val] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:500:6: ^( HexIntConst[$val] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(HexIntConst, val), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 4 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:501:4: val= FloatNumber
					{
					val=(Token)match(input,FloatNumber,FOLLOW_FloatNumber_in_noSignFactor2099);  
					stream_FloatNumber.add(val);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 502:7: -> ^( FloatConst[$val] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:502:10: ^( FloatConst[$val] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FloatConst, val), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 5 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:503:4: val= BooleanLiteral
					{
					val=(Token)match(input,BooleanLiteral,FOLLOW_BooleanLiteral_in_noSignFactor2122);  
					stream_BooleanLiteral.add(val);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 504:3: -> ^( BooleanConst[$val] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:504:6: ^( BooleanConst[$val] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BooleanConst, val), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 6 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:505:4: kw= 'null'
					{
					kw=(Token)match(input,94,FOLLOW_94_in_noSignFactor2141);  
					stream_94.add(kw);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 506:3: -> ^( NullConst[$kw, \"NullConst\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:506:6: ^( NullConst[$kw, \"NullConst\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(NullConst, kw, "NullConst"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 7 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:507:6: target= identAccess
					{
					pushFollow(FOLLOW_identAccess_in_noSignFactor2161);
					target=identAccess();
					state._fsp--;

					stream_identAccess.add(target.getTree());
					// AST REWRITE
					// elements: identAccess
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 508:7: -> identAccess
					{
						adaptor.addChild(root_0, stream_identAccess.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 8 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:509:6: '(' expr ')'
					{
					char_literal98=(Token)match(input,69,FOLLOW_69_in_noSignFactor2180);  
					stream_69.add(char_literal98);

					pushFollow(FOLLOW_expr_in_noSignFactor2182);
					expr99=expr();
					state._fsp--;

					stream_expr.add(expr99.getTree());
					char_literal100=(Token)match(input,70,FOLLOW_70_in_noSignFactor2184);  
					stream_70.add(char_literal100);

					// AST REWRITE
					// elements: expr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 510:7: -> expr
					{
						adaptor.addChild(root_0, stream_expr.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 9 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:511:4: lp= '(' referenceType ')' noSignFactor
					{
					lp=(Token)match(input,69,FOLLOW_69_in_noSignFactor2201);  
					stream_69.add(lp);

					pushFollow(FOLLOW_referenceType_in_noSignFactor2203);
					referenceType101=referenceType();
					state._fsp--;

					stream_referenceType.add(referenceType101.getTree());
					char_literal102=(Token)match(input,70,FOLLOW_70_in_noSignFactor2205);  
					stream_70.add(char_literal102);

					pushFollow(FOLLOW_noSignFactor_in_noSignFactor2207);
					noSignFactor103=noSignFactor();
					state._fsp--;

					stream_noSignFactor.add(noSignFactor103.getTree());
					// AST REWRITE
					// elements: referenceType, noSignFactor
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 512:3: -> ^( Cast[$lp, \"Cast\"] noSignFactor referenceType )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:512:6: ^( Cast[$lp, \"Cast\"] noSignFactor referenceType )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Cast, lp, "Cast"), root_1);
						adaptor.addChild(root_1, stream_noSignFactor.nextTree());
						adaptor.addChild(root_1, stream_referenceType.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "noSignFactor"


	public static class identAccess_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "identAccess"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:515:1: identAccess : (var= Identifier -> ^( Var[$var, \"Var\"] Identifier ) |var= Identifier methodCallTail -> ^( MethodCall[$var, \"MethodCall\"] ^( ThisRef[$var, \"ThisRef\"] ) Identifier ( methodCallTail )? ) |kw= 'this' -> ^( ThisRef[$kw, \"ThisRef\"] ) ) ( selectorSeq[$identAccess.tree] -> selectorSeq )? ;
	public final JavaliParser.identAccess_return identAccess() throws RecognitionException {
		JavaliParser.identAccess_return retval = new JavaliParser.identAccess_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token var=null;
		Token kw=null;
		ParserRuleReturnScope methodCallTail104 =null;
		ParserRuleReturnScope selectorSeq105 =null;

		Object var_tree=null;
		Object kw_tree=null;
		RewriteRuleTokenStream stream_98=new RewriteRuleTokenStream(adaptor,"token 98");
		RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
		RewriteRuleSubtreeStream stream_selectorSeq=new RewriteRuleSubtreeStream(adaptor,"rule selectorSeq");
		RewriteRuleSubtreeStream stream_methodCallTail=new RewriteRuleSubtreeStream(adaptor,"rule methodCallTail");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:516:4: ( (var= Identifier -> ^( Var[$var, \"Var\"] Identifier ) |var= Identifier methodCallTail -> ^( MethodCall[$var, \"MethodCall\"] ^( ThisRef[$var, \"ThisRef\"] ) Identifier ( methodCallTail )? ) |kw= 'this' -> ^( ThisRef[$kw, \"ThisRef\"] ) ) ( selectorSeq[$identAccess.tree] -> selectorSeq )? )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:516:6: (var= Identifier -> ^( Var[$var, \"Var\"] Identifier ) |var= Identifier methodCallTail -> ^( MethodCall[$var, \"MethodCall\"] ^( ThisRef[$var, \"ThisRef\"] ) Identifier ( methodCallTail )? ) |kw= 'this' -> ^( ThisRef[$kw, \"ThisRef\"] ) ) ( selectorSeq[$identAccess.tree] -> selectorSeq )?
			{
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:516:6: (var= Identifier -> ^( Var[$var, \"Var\"] Identifier ) |var= Identifier methodCallTail -> ^( MethodCall[$var, \"MethodCall\"] ^( ThisRef[$var, \"ThisRef\"] ) Identifier ( methodCallTail )? ) |kw= 'this' -> ^( ThisRef[$kw, \"ThisRef\"] ) )
			int alt33=3;
			int LA33_0 = input.LA(1);
			if ( (LA33_0==Identifier) ) {
				int LA33_1 = input.LA(2);
				if ( ((LA33_1 >= 66 && LA33_1 <= 68)||(LA33_1 >= 70 && LA33_1 <= 85)||LA33_1==105) ) {
					alt33=1;
				}
				else if ( (LA33_1==69) ) {
					alt33=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 33, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA33_0==98) ) {
				alt33=3;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 33, 0, input);
				throw nvae;
			}

			switch (alt33) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:516:8: var= Identifier
					{
					var=(Token)match(input,Identifier,FOLLOW_Identifier_in_identAccess2239);  
					stream_Identifier.add(var);

					// AST REWRITE
					// elements: Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 517:7: -> ^( Var[$var, \"Var\"] Identifier )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:517:10: ^( Var[$var, \"Var\"] Identifier )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Var, var, "Var"), root_1);
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:518:9: var= Identifier methodCallTail
					{
					var=(Token)match(input,Identifier,FOLLOW_Identifier_in_identAccess2269);  
					stream_Identifier.add(var);

					pushFollow(FOLLOW_methodCallTail_in_identAccess2271);
					methodCallTail104=methodCallTail();
					state._fsp--;

					stream_methodCallTail.add(methodCallTail104.getTree());
					// AST REWRITE
					// elements: methodCallTail, Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 519:10: -> ^( MethodCall[$var, \"MethodCall\"] ^( ThisRef[$var, \"ThisRef\"] ) Identifier ( methodCallTail )? )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:519:13: ^( MethodCall[$var, \"MethodCall\"] ^( ThisRef[$var, \"ThisRef\"] ) Identifier ( methodCallTail )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MethodCall, var, "MethodCall"), root_1);
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:519:47: ^( ThisRef[$var, \"ThisRef\"] )
						{
						Object root_2 = (Object)adaptor.nil();
						root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(ThisRef, var, "ThisRef"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_1, stream_Identifier.nextNode());
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:519:86: ( methodCallTail )?
						if ( stream_methodCallTail.hasNext() ) {
							adaptor.addChild(root_1, stream_methodCallTail.nextTree());
						}
						stream_methodCallTail.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:520:9: kw= 'this'
					{
					kw=(Token)match(input,98,FOLLOW_98_in_identAccess2312);  
					stream_98.add(kw);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 521:10: -> ^( ThisRef[$kw, \"ThisRef\"] )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:521:13: ^( ThisRef[$kw, \"ThisRef\"] )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ThisRef, kw, "ThisRef"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:523:7: ( selectorSeq[$identAccess.tree] -> selectorSeq )?
			int alt34=2;
			int LA34_0 = input.LA(1);
			if ( (LA34_0==75||LA34_0==84) ) {
				alt34=1;
			}
			switch (alt34) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:523:10: selectorSeq[$identAccess.tree]
					{
					pushFollow(FOLLOW_selectorSeq_in_identAccess2351);
					selectorSeq105=selectorSeq(retval.tree);
					state._fsp--;

					stream_selectorSeq.add(selectorSeq105.getTree());
					// AST REWRITE
					// elements: selectorSeq
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 523:41: -> selectorSeq
					{
						adaptor.addChild(root_0, stream_selectorSeq.nextTree());
					}


					retval.tree = root_0;

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "identAccess"


	public static class selectorSeq_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "selectorSeq"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:526:1: selectorSeq[Object target] : ( ( fieldSelector[$target] -> fieldSelector | elemSelector[$target] -> elemSelector ) ) ( fieldSelector[$selectorSeq.tree] -> fieldSelector | elemSelector[$selectorSeq.tree] -> elemSelector )* ;
	public final JavaliParser.selectorSeq_return selectorSeq(Object target) throws RecognitionException {
		JavaliParser.selectorSeq_return retval = new JavaliParser.selectorSeq_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope fieldSelector106 =null;
		ParserRuleReturnScope elemSelector107 =null;
		ParserRuleReturnScope fieldSelector108 =null;
		ParserRuleReturnScope elemSelector109 =null;

		RewriteRuleSubtreeStream stream_elemSelector=new RewriteRuleSubtreeStream(adaptor,"rule elemSelector");
		RewriteRuleSubtreeStream stream_fieldSelector=new RewriteRuleSubtreeStream(adaptor,"rule fieldSelector");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:527:4: ( ( ( fieldSelector[$target] -> fieldSelector | elemSelector[$target] -> elemSelector ) ) ( fieldSelector[$selectorSeq.tree] -> fieldSelector | elemSelector[$selectorSeq.tree] -> elemSelector )* )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:528:7: ( ( fieldSelector[$target] -> fieldSelector | elemSelector[$target] -> elemSelector ) ) ( fieldSelector[$selectorSeq.tree] -> fieldSelector | elemSelector[$selectorSeq.tree] -> elemSelector )*
			{
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:528:7: ( ( fieldSelector[$target] -> fieldSelector | elemSelector[$target] -> elemSelector ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:528:9: ( fieldSelector[$target] -> fieldSelector | elemSelector[$target] -> elemSelector )
			{
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:528:9: ( fieldSelector[$target] -> fieldSelector | elemSelector[$target] -> elemSelector )
			int alt35=2;
			int LA35_0 = input.LA(1);
			if ( (LA35_0==75) ) {
				alt35=1;
			}
			else if ( (LA35_0==84) ) {
				alt35=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 35, 0, input);
				throw nvae;
			}

			switch (alt35) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:528:11: fieldSelector[$target]
					{
					pushFollow(FOLLOW_fieldSelector_in_selectorSeq2390);
					fieldSelector106=fieldSelector(target);
					state._fsp--;

					stream_fieldSelector.add(fieldSelector106.getTree());
					// AST REWRITE
					// elements: fieldSelector
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 528:34: -> fieldSelector
					{
						adaptor.addChild(root_0, stream_fieldSelector.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:528:53: elemSelector[$target]
					{
					pushFollow(FOLLOW_elemSelector_in_selectorSeq2399);
					elemSelector107=elemSelector(target);
					state._fsp--;

					stream_elemSelector.add(elemSelector107.getTree());
					// AST REWRITE
					// elements: elemSelector
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 528:75: -> elemSelector
					{
						adaptor.addChild(root_0, stream_elemSelector.nextTree());
					}


					retval.tree = root_0;

					}
					break;

			}

			}

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:529:7: ( fieldSelector[$selectorSeq.tree] -> fieldSelector | elemSelector[$selectorSeq.tree] -> elemSelector )*
			loop36:
			while (true) {
				int alt36=3;
				int LA36_0 = input.LA(1);
				if ( (LA36_0==75) ) {
					alt36=1;
				}
				else if ( (LA36_0==84) ) {
					alt36=2;
				}

				switch (alt36) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:529:10: fieldSelector[$selectorSeq.tree]
					{
					pushFollow(FOLLOW_fieldSelector_in_selectorSeq2419);
					fieldSelector108=fieldSelector(retval.tree);
					state._fsp--;

					stream_fieldSelector.add(fieldSelector108.getTree());
					// AST REWRITE
					// elements: fieldSelector
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 530:10: -> fieldSelector
					{
						adaptor.addChild(root_0, stream_fieldSelector.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:531:10: elemSelector[$selectorSeq.tree]
					{
					pushFollow(FOLLOW_elemSelector_in_selectorSeq2444);
					elemSelector109=elemSelector(retval.tree);
					state._fsp--;

					stream_elemSelector.add(elemSelector109.getTree());
					// AST REWRITE
					// elements: elemSelector
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 532:10: -> elemSelector
					{
						adaptor.addChild(root_0, stream_elemSelector.nextTree());
					}


					retval.tree = root_0;

					}
					break;

				default :
					break loop36;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "selectorSeq"


	public static class fieldSelector_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "fieldSelector"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:536:1: fieldSelector[Object target] : ( '.' id= Identifier methodCallTail -> ^( MethodCall[$id, \"MethodCall\"] Identifier ( methodCallTail )? ) | '.' id= Identifier -> ^( Field[$id, \"Field\"] Identifier ) );
	public final JavaliParser.fieldSelector_return fieldSelector(Object target) throws RecognitionException {
		JavaliParser.fieldSelector_return retval = new JavaliParser.fieldSelector_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token id=null;
		Token char_literal110=null;
		Token char_literal112=null;
		ParserRuleReturnScope methodCallTail111 =null;

		Object id_tree=null;
		Object char_literal110_tree=null;
		Object char_literal112_tree=null;
		RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
		RewriteRuleTokenStream stream_75=new RewriteRuleTokenStream(adaptor,"token 75");
		RewriteRuleSubtreeStream stream_methodCallTail=new RewriteRuleSubtreeStream(adaptor,"rule methodCallTail");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:537:4: ( '.' id= Identifier methodCallTail -> ^( MethodCall[$id, \"MethodCall\"] Identifier ( methodCallTail )? ) | '.' id= Identifier -> ^( Field[$id, \"Field\"] Identifier ) )
			int alt37=2;
			int LA37_0 = input.LA(1);
			if ( (LA37_0==75) ) {
				int LA37_1 = input.LA(2);
				if ( (LA37_1==Identifier) ) {
					int LA37_2 = input.LA(3);
					if ( (LA37_2==69) ) {
						alt37=1;
					}
					else if ( ((LA37_2 >= 66 && LA37_2 <= 68)||(LA37_2 >= 70 && LA37_2 <= 85)||LA37_2==105) ) {
						alt37=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 37, 2, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 37, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 37, 0, input);
				throw nvae;
			}

			switch (alt37) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:537:6: '.' id= Identifier methodCallTail
					{
					char_literal110=(Token)match(input,75,FOLLOW_75_in_fieldSelector2482);  
					stream_75.add(char_literal110);

					id=(Token)match(input,Identifier,FOLLOW_Identifier_in_fieldSelector2486);  
					stream_Identifier.add(id);

					pushFollow(FOLLOW_methodCallTail_in_fieldSelector2488);
					methodCallTail111=methodCallTail();
					state._fsp--;

					stream_methodCallTail.add(methodCallTail111.getTree());
					// AST REWRITE
					// elements: Identifier, methodCallTail
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 538:7: -> ^( MethodCall[$id, \"MethodCall\"] Identifier ( methodCallTail )? )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:538:10: ^( MethodCall[$id, \"MethodCall\"] Identifier ( methodCallTail )? )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MethodCall, id, "MethodCall"), root_1);
						adaptor.addChild(root_1,  target );
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:538:67: ( methodCallTail )?
						if ( stream_methodCallTail.hasNext() ) {
							adaptor.addChild(root_1, stream_methodCallTail.nextTree());
						}
						stream_methodCallTail.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:539:6: '.' id= Identifier
					{
					char_literal112=(Token)match(input,75,FOLLOW_75_in_fieldSelector2519);  
					stream_75.add(char_literal112);

					id=(Token)match(input,Identifier,FOLLOW_Identifier_in_fieldSelector2523);  
					stream_Identifier.add(id);

					// AST REWRITE
					// elements: Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 540:7: -> ^( Field[$id, \"Field\"] Identifier )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:540:10: ^( Field[$id, \"Field\"] Identifier )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Field, id, "Field"), root_1);
						adaptor.addChild(root_1,  target );
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "fieldSelector"


	public static class elemSelector_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "elemSelector"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:543:1: elemSelector[Object target] : kw= '[' iExpr= simpleExpr ']' -> ^( Index[$kw, \"Index\"] simpleExpr ) ;
	public final JavaliParser.elemSelector_return elemSelector(Object target) throws RecognitionException {
		JavaliParser.elemSelector_return retval = new JavaliParser.elemSelector_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token kw=null;
		Token char_literal113=null;
		ParserRuleReturnScope iExpr =null;

		Object kw_tree=null;
		Object char_literal113_tree=null;
		RewriteRuleTokenStream stream_84=new RewriteRuleTokenStream(adaptor,"token 84");
		RewriteRuleTokenStream stream_85=new RewriteRuleTokenStream(adaptor,"token 85");
		RewriteRuleSubtreeStream stream_simpleExpr=new RewriteRuleSubtreeStream(adaptor,"rule simpleExpr");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:544:4: (kw= '[' iExpr= simpleExpr ']' -> ^( Index[$kw, \"Index\"] simpleExpr ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:544:7: kw= '[' iExpr= simpleExpr ']'
			{
			kw=(Token)match(input,84,FOLLOW_84_in_elemSelector2562);  
			stream_84.add(kw);

			pushFollow(FOLLOW_simpleExpr_in_elemSelector2566);
			iExpr=simpleExpr();
			state._fsp--;

			stream_simpleExpr.add(iExpr.getTree());
			char_literal113=(Token)match(input,85,FOLLOW_85_in_elemSelector2568);  
			stream_85.add(char_literal113);

			// AST REWRITE
			// elements: simpleExpr
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 545:7: -> ^( Index[$kw, \"Index\"] simpleExpr )
			{
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:545:10: ^( Index[$kw, \"Index\"] simpleExpr )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Index, kw, "Index"), root_1);
				adaptor.addChild(root_1,  target );
				adaptor.addChild(root_1, stream_simpleExpr.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "elemSelector"


	public static class type_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "type"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:550:1: type : ( primitiveType | referenceType );
	public final JavaliParser.type_return type() throws RecognitionException {
		JavaliParser.type_return retval = new JavaliParser.type_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope primitiveType114 =null;
		ParserRuleReturnScope referenceType115 =null;


		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:551:2: ( primitiveType | referenceType )
			int alt38=2;
			switch ( input.LA(1) ) {
			case 92:
				{
				int LA38_1 = input.LA(2);
				if ( (LA38_1==Identifier) ) {
					alt38=1;
				}
				else if ( (LA38_1==84) ) {
					alt38=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 38, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 90:
				{
				int LA38_2 = input.LA(2);
				if ( (LA38_2==Identifier) ) {
					alt38=1;
				}
				else if ( (LA38_2==84) ) {
					alt38=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 38, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 86:
				{
				int LA38_3 = input.LA(2);
				if ( (LA38_3==Identifier) ) {
					alt38=1;
				}
				else if ( (LA38_3==84) ) {
					alt38=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 38, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case Identifier:
				{
				alt38=2;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 38, 0, input);
				throw nvae;
			}
			switch (alt38) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:551:4: primitiveType
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_primitiveType_in_type2602);
					primitiveType114=primitiveType();
					state._fsp--;

					adaptor.addChild(root_0, primitiveType114.getTree());

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:552:4: referenceType
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_referenceType_in_type2607);
					referenceType115=referenceType();
					state._fsp--;

					adaptor.addChild(root_0, referenceType115.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "type"


	public static class referenceType_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "referenceType"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:555:1: referenceType : ( Identifier | arrayType );
	public final JavaliParser.referenceType_return referenceType() throws RecognitionException {
		JavaliParser.referenceType_return retval = new JavaliParser.referenceType_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token Identifier116=null;
		ParserRuleReturnScope arrayType117 =null;

		Object Identifier116_tree=null;

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:556:2: ( Identifier | arrayType )
			int alt39=2;
			int LA39_0 = input.LA(1);
			if ( (LA39_0==Identifier) ) {
				int LA39_1 = input.LA(2);
				if ( (LA39_1==84) ) {
					alt39=2;
				}
				else if ( (LA39_1==Identifier||LA39_1==70) ) {
					alt39=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 39, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA39_0==86||LA39_0==90||LA39_0==92) ) {
				alt39=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 39, 0, input);
				throw nvae;
			}

			switch (alt39) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:556:4: Identifier
					{
					root_0 = (Object)adaptor.nil();


					Identifier116=(Token)match(input,Identifier,FOLLOW_Identifier_in_referenceType2618); 
					Identifier116_tree = (Object)adaptor.create(Identifier116);
					adaptor.addChild(root_0, Identifier116_tree);

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:557:4: arrayType
					{
					root_0 = (Object)adaptor.nil();


					pushFollow(FOLLOW_arrayType_in_referenceType2623);
					arrayType117=arrayType();
					state._fsp--;

					adaptor.addChild(root_0, arrayType117.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "referenceType"


	public static class primitiveType_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "primitiveType"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:560:1: primitiveType : (tok= 'int' -> Identifier[$tok, $tok.text] |tok= 'float' -> Identifier[$tok, $tok.text] |tok= 'boolean' -> Identifier[$tok, $tok.text] );
	public final JavaliParser.primitiveType_return primitiveType() throws RecognitionException {
		JavaliParser.primitiveType_return retval = new JavaliParser.primitiveType_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token tok=null;

		Object tok_tree=null;
		RewriteRuleTokenStream stream_92=new RewriteRuleTokenStream(adaptor,"token 92");
		RewriteRuleTokenStream stream_90=new RewriteRuleTokenStream(adaptor,"token 90");
		RewriteRuleTokenStream stream_86=new RewriteRuleTokenStream(adaptor,"token 86");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:561:2: (tok= 'int' -> Identifier[$tok, $tok.text] |tok= 'float' -> Identifier[$tok, $tok.text] |tok= 'boolean' -> Identifier[$tok, $tok.text] )
			int alt40=3;
			switch ( input.LA(1) ) {
			case 92:
				{
				alt40=1;
				}
				break;
			case 90:
				{
				alt40=2;
				}
				break;
			case 86:
				{
				alt40=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 40, 0, input);
				throw nvae;
			}
			switch (alt40) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:561:4: tok= 'int'
					{
					tok=(Token)match(input,92,FOLLOW_92_in_primitiveType2636);  
					stream_92.add(tok);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 562:3: -> Identifier[$tok, $tok.text]
					{
						adaptor.addChild(root_0, (Object)adaptor.create(Identifier, tok, (tok!=null?tok.getText():null)));
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:563:5: tok= 'float'
					{
					tok=(Token)match(input,90,FOLLOW_90_in_primitiveType2651);  
					stream_90.add(tok);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 564:7: -> Identifier[$tok, $tok.text]
					{
						adaptor.addChild(root_0, (Object)adaptor.create(Identifier, tok, (tok!=null?tok.getText():null)));
					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:565:4: tok= 'boolean'
					{
					tok=(Token)match(input,86,FOLLOW_86_in_primitiveType2669);  
					stream_86.add(tok);

					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 566:3: -> Identifier[$tok, $tok.text]
					{
						adaptor.addChild(root_0, (Object)adaptor.create(Identifier, tok, (tok!=null?tok.getText():null)));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "primitiveType"


	public static class arrayType_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "arrayType"
	// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:569:1: arrayType : (id= Identifier '[' ']' -> ^( ArrayType[$id, \"ArrayType\"] Identifier ) |pt= primitiveType '[' ']' -> ^( ArrayType[$pt.start, \"ArrayType\"] primitiveType ) );
	public final JavaliParser.arrayType_return arrayType() throws RecognitionException {
		JavaliParser.arrayType_return retval = new JavaliParser.arrayType_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token id=null;
		Token char_literal118=null;
		Token char_literal119=null;
		Token char_literal120=null;
		Token char_literal121=null;
		ParserRuleReturnScope pt =null;

		Object id_tree=null;
		Object char_literal118_tree=null;
		Object char_literal119_tree=null;
		Object char_literal120_tree=null;
		Object char_literal121_tree=null;
		RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
		RewriteRuleTokenStream stream_84=new RewriteRuleTokenStream(adaptor,"token 84");
		RewriteRuleTokenStream stream_85=new RewriteRuleTokenStream(adaptor,"token 85");
		RewriteRuleSubtreeStream stream_primitiveType=new RewriteRuleSubtreeStream(adaptor,"rule primitiveType");

		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:570:2: (id= Identifier '[' ']' -> ^( ArrayType[$id, \"ArrayType\"] Identifier ) |pt= primitiveType '[' ']' -> ^( ArrayType[$pt.start, \"ArrayType\"] primitiveType ) )
			int alt41=2;
			int LA41_0 = input.LA(1);
			if ( (LA41_0==Identifier) ) {
				alt41=1;
			}
			else if ( (LA41_0==86||LA41_0==90||LA41_0==92) ) {
				alt41=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 41, 0, input);
				throw nvae;
			}

			switch (alt41) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:570:4: id= Identifier '[' ']'
					{
					id=(Token)match(input,Identifier,FOLLOW_Identifier_in_arrayType2689);  
					stream_Identifier.add(id);

					char_literal118=(Token)match(input,84,FOLLOW_84_in_arrayType2691);  
					stream_84.add(char_literal118);

					char_literal119=(Token)match(input,85,FOLLOW_85_in_arrayType2693);  
					stream_85.add(char_literal119);

					// AST REWRITE
					// elements: Identifier
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 571:3: -> ^( ArrayType[$id, \"ArrayType\"] Identifier )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:571:6: ^( ArrayType[$id, \"ArrayType\"] Identifier )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ArrayType, id, "ArrayType"), root_1);
						adaptor.addChild(root_1, stream_Identifier.nextNode());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:572:4: pt= primitiveType '[' ']'
					{
					pushFollow(FOLLOW_primitiveType_in_arrayType2713);
					pt=primitiveType();
					state._fsp--;

					stream_primitiveType.add(pt.getTree());
					char_literal120=(Token)match(input,84,FOLLOW_84_in_arrayType2715);  
					stream_84.add(char_literal120);

					char_literal121=(Token)match(input,85,FOLLOW_85_in_arrayType2717);  
					stream_85.add(char_literal121);

					// AST REWRITE
					// elements: primitiveType
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 573:3: -> ^( ArrayType[$pt.start, \"ArrayType\"] primitiveType )
					{
						// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:573:6: ^( ArrayType[$pt.start, \"ArrayType\"] primitiveType )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ArrayType, (pt!=null?(pt.start):null), "ArrayType"), root_1);
						adaptor.addChild(root_1, stream_primitiveType.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}

		catch (RecognitionException re) {
			reportError(re);
			throw re;
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "arrayType"

	// Delegated rules



	public static final BitSet FOLLOW_classDecl_in_unit267 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
	public static final BitSet FOLLOW_EOF_in_unit270 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_87_in_classDecl291 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_classDecl293 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_104_in_classDecl295 = new BitSet(new long[]{0x0000010000000000L,0x0000040814400000L});
	public static final BitSet FOLLOW_declList_in_classDecl297 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_106_in_classDecl300 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_87_in_classDecl328 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_classDecl330 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_89_in_classDecl332 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_classDecl334 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_104_in_classDecl336 = new BitSet(new long[]{0x0000010000000000L,0x0000040814400000L});
	public static final BitSet FOLLOW_declList_in_classDecl338 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_106_in_classDecl341 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_varDecl_in_declList372 = new BitSet(new long[]{0x0000010000000002L,0x0000000814400000L});
	public static final BitSet FOLLOW_methodDecl_in_declList376 = new BitSet(new long[]{0x0000010000000002L,0x0000000814400000L});
	public static final BitSet FOLLOW_type_in_varDecl392 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_varDecl394 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_77_in_varDecl396 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_varDecl418 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_varDecl420 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_73_in_varDecl424 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_varDecl426 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002200L});
	public static final BitSet FOLLOW_77_in_varDecl431 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_methodHeading_in_methodDecl460 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_methodBody_in_methodDecl462 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_methodHeading491 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_methodHeading493 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_methodHeading495 = new BitSet(new long[]{0x0000010000000000L,0x0000000014400040L});
	public static final BitSet FOLLOW_formalParamList_in_methodHeading497 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_methodHeading500 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_99_in_methodHeading524 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_methodHeading526 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_methodHeading528 = new BitSet(new long[]{0x0000010000000000L,0x0000000014400040L});
	public static final BitSet FOLLOW_formalParamList_in_methodHeading530 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_methodHeading533 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_formalParamList564 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_formalParamList566 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_73_in_formalParamList570 = new BitSet(new long[]{0x0000010000000000L,0x0000000014400000L});
	public static final BitSet FOLLOW_type_in_formalParamList572 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_formalParamList574 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_methodBodyWithDeclList_in_methodBody604 = new BitSet(new long[]{0x0000010000000000L,0x000004F608000000L});
	public static final BitSet FOLLOW_stmtList_in_methodBody613 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_106_in_methodBody615 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_106_in_methodBody646 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_methodBody678 = new BitSet(new long[]{0x0000010000000000L,0x000000F608000000L});
	public static final BitSet FOLLOW_stmtList_in_methodBody682 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_106_in_methodBody684 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_methodBody718 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_106_in_methodBody721 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_methodBodyWithDeclList759 = new BitSet(new long[]{0x0000010000000000L,0x0000000814400000L});
	public static final BitSet FOLLOW_declList_in_methodBodyWithDeclList763 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_stmt_in_stmtList789 = new BitSet(new long[]{0x0000010000000002L,0x000000F608000000L});
	public static final BitSet FOLLOW_assignmentOrMethodCall_in_stmt808 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_77_in_stmt810 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ioStmt_in_stmt824 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ifStmt_in_stmt829 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_whileStmt_in_stmt834 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_returnStmt_in_stmt840 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identAccess_in_assignmentOrMethodCall857 = new BitSet(new long[]{0x0000000000000002L,0x0000000000010000L});
	public static final BitSet FOLLOW_assignmentTail_in_assignmentOrMethodCall868 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_80_in_assignmentTail929 = new BitSet(new long[]{0x0000014880200000L,0x00000005E0000522L});
	public static final BitSet FOLLOW_assignmentRHS_in_assignmentTail933 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expr_in_assignmentRHS961 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_newExpr_in_assignmentRHS965 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_readExpr_in_assignmentRHS969 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_readExprFloat_in_assignmentRHS973 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_69_in_methodCallTail986 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000562L});
	public static final BitSet FOLLOW_actualParamList_in_methodCallTail988 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_methodCallTail991 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expr_in_actualParamList1044 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_73_in_actualParamList1048 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_expr_in_actualParamList1050 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_101_in_ioStmt1074 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_ioStmt1076 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_expr_in_ioStmt1078 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_ioStmt1080 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_77_in_ioStmt1082 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_102_in_ioStmt1106 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_ioStmt1108 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_expr_in_ioStmt1110 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_ioStmt1112 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_77_in_ioStmt1114 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_103_in_ioStmt1140 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_ioStmt1142 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_ioStmt1144 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_77_in_ioStmt1146 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_91_in_ifStmt1177 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_ifStmt1179 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_expr_in_ifStmt1181 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_ifStmt1183 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_stmtBlock_in_ifStmt1187 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
	public static final BitSet FOLLOW_88_in_ifStmt1222 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_stmtBlock_in_ifStmt1226 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_100_in_whileStmt1263 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_whileStmt1265 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_expr_in_whileStmt1267 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_whileStmt1269 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_stmtBlock_in_whileStmt1271 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_97_in_returnStmt1298 = new BitSet(new long[]{0x0000014880200000L,0x0000000440002522L});
	public static final BitSet FOLLOW_expr_in_returnStmt1300 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_77_in_returnStmt1303 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_104_in_stmtBlock1334 = new BitSet(new long[]{0x0000010000000000L,0x000004F608000000L});
	public static final BitSet FOLLOW_stmtList_in_stmtBlock1336 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_106_in_stmtBlock1339 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_93_in_newExpr1369 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_newExpr1371 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_newExpr1373 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_newExpr1375 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_93_in_newExpr1395 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_newExpr1399 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_84_in_newExpr1401 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_simpleExpr_in_newExpr1403 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
	public static final BitSet FOLLOW_85_in_newExpr1405 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_93_in_newExpr1428 = new BitSet(new long[]{0x0000000000000000L,0x0000000014400000L});
	public static final BitSet FOLLOW_primitiveType_in_newExpr1432 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_84_in_newExpr1434 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_simpleExpr_in_newExpr1436 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
	public static final BitSet FOLLOW_85_in_newExpr1438 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_95_in_readExpr1467 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_readExpr1469 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_readExpr1471 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_96_in_readExprFloat1497 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_69_in_readExprFloat1499 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_readExprFloat1501 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simpleExpr_in_expr1531 = new BitSet(new long[]{0x0000000000000002L,0x00000000000EC004L});
	public static final BitSet FOLLOW_compOp_in_expr1552 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_simpleExpr_in_expr1556 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_81_in_compOp1593 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_66_in_compOp1611 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_78_in_compOp1629 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_79_in_compOp1647 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_82_in_compOp1665 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_83_in_compOp1683 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_simpleExpr1710 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000500L});
	public static final BitSet FOLLOW_weakOp_in_simpleExpr1724 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_term_in_simpleExpr1728 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000500L});
	public static final BitSet FOLLOW_72_in_weakOp1766 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_74_in_weakOp1784 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_105_in_weakOp1802 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_factor_in_term1829 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001098L});
	public static final BitSet FOLLOW_strongOp_in_term1843 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_factor_in_term1847 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001098L});
	public static final BitSet FOLLOW_71_in_strongOp1885 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_76_in_strongOp1903 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_67_in_strongOp1921 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_68_in_strongOp1939 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_72_in_factor1963 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000022L});
	public static final BitSet FOLLOW_noSignFactor_in_factor1965 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_74_in_factor1992 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000022L});
	public static final BitSet FOLLOW_noSignFactor_in_factor1994 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_noSignFactor_in_factor2019 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_65_in_noSignFactor2032 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_factor_in_noSignFactor2034 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DecimalNumber_in_noSignFactor2061 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_HexNumber_in_noSignFactor2080 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FloatNumber_in_noSignFactor2099 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BooleanLiteral_in_noSignFactor2122 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_94_in_noSignFactor2141 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identAccess_in_noSignFactor2161 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_69_in_noSignFactor2180 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_expr_in_noSignFactor2182 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_noSignFactor2184 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_69_in_noSignFactor2201 = new BitSet(new long[]{0x0000010000000000L,0x0000000014400000L});
	public static final BitSet FOLLOW_referenceType_in_noSignFactor2203 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_70_in_noSignFactor2205 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000022L});
	public static final BitSet FOLLOW_noSignFactor_in_noSignFactor2207 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_Identifier_in_identAccess2239 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100800L});
	public static final BitSet FOLLOW_Identifier_in_identAccess2269 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_methodCallTail_in_identAccess2271 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100800L});
	public static final BitSet FOLLOW_98_in_identAccess2312 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100800L});
	public static final BitSet FOLLOW_selectorSeq_in_identAccess2351 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldSelector_in_selectorSeq2390 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100800L});
	public static final BitSet FOLLOW_elemSelector_in_selectorSeq2399 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100800L});
	public static final BitSet FOLLOW_fieldSelector_in_selectorSeq2419 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100800L});
	public static final BitSet FOLLOW_elemSelector_in_selectorSeq2444 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100800L});
	public static final BitSet FOLLOW_75_in_fieldSelector2482 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_fieldSelector2486 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
	public static final BitSet FOLLOW_methodCallTail_in_fieldSelector2488 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_75_in_fieldSelector2519 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_Identifier_in_fieldSelector2523 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_84_in_elemSelector2562 = new BitSet(new long[]{0x0000014880200000L,0x0000000440000522L});
	public static final BitSet FOLLOW_simpleExpr_in_elemSelector2566 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
	public static final BitSet FOLLOW_85_in_elemSelector2568 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primitiveType_in_type2602 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_referenceType_in_type2607 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_Identifier_in_referenceType2618 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arrayType_in_referenceType2623 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_92_in_primitiveType2636 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_90_in_primitiveType2651 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_86_in_primitiveType2669 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_Identifier_in_arrayType2689 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_84_in_arrayType2691 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
	public static final BitSet FOLLOW_85_in_arrayType2693 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primitiveType_in_arrayType2713 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_84_in_arrayType2715 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
	public static final BitSet FOLLOW_85_in_arrayType2717 = new BitSet(new long[]{0x0000000000000002L});
}
