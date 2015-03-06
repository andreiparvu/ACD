// $ANTLR 3.5.2 /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g 2015-03-04 16:03:04

package cd.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class JavaliLexer extends Lexer {
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
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public JavaliLexer() {} 
	public JavaliLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public JavaliLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "/home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g"; }

	// $ANTLR start "T__65"
	public final void mT__65() throws RecognitionException {
		try {
			int _type = T__65;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:6:7: ( '!' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:6:9: '!'
			{
			match('!'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__65"

	// $ANTLR start "T__66"
	public final void mT__66() throws RecognitionException {
		try {
			int _type = T__66;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:7:7: ( '!=' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:7:9: '!='
			{
			match("!="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__66"

	// $ANTLR start "T__67"
	public final void mT__67() throws RecognitionException {
		try {
			int _type = T__67;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:8:7: ( '%' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:8:9: '%'
			{
			match('%'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__67"

	// $ANTLR start "T__68"
	public final void mT__68() throws RecognitionException {
		try {
			int _type = T__68;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:9:7: ( '&&' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:9:9: '&&'
			{
			match("&&"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__68"

	// $ANTLR start "T__69"
	public final void mT__69() throws RecognitionException {
		try {
			int _type = T__69;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:10:7: ( '(' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:10:9: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__69"

	// $ANTLR start "T__70"
	public final void mT__70() throws RecognitionException {
		try {
			int _type = T__70;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:11:7: ( ')' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:11:9: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__70"

	// $ANTLR start "T__71"
	public final void mT__71() throws RecognitionException {
		try {
			int _type = T__71;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:12:7: ( '*' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:12:9: '*'
			{
			match('*'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__71"

	// $ANTLR start "T__72"
	public final void mT__72() throws RecognitionException {
		try {
			int _type = T__72;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:13:7: ( '+' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:13:9: '+'
			{
			match('+'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__72"

	// $ANTLR start "T__73"
	public final void mT__73() throws RecognitionException {
		try {
			int _type = T__73;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:14:7: ( ',' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:14:9: ','
			{
			match(','); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__73"

	// $ANTLR start "T__74"
	public final void mT__74() throws RecognitionException {
		try {
			int _type = T__74;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:15:7: ( '-' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:15:9: '-'
			{
			match('-'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__74"

	// $ANTLR start "T__75"
	public final void mT__75() throws RecognitionException {
		try {
			int _type = T__75;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:16:7: ( '.' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:16:9: '.'
			{
			match('.'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__75"

	// $ANTLR start "T__76"
	public final void mT__76() throws RecognitionException {
		try {
			int _type = T__76;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:17:7: ( '/' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:17:9: '/'
			{
			match('/'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__76"

	// $ANTLR start "T__77"
	public final void mT__77() throws RecognitionException {
		try {
			int _type = T__77;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:18:7: ( ';' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:18:9: ';'
			{
			match(';'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__77"

	// $ANTLR start "T__78"
	public final void mT__78() throws RecognitionException {
		try {
			int _type = T__78;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:19:7: ( '<' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:19:9: '<'
			{
			match('<'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__78"

	// $ANTLR start "T__79"
	public final void mT__79() throws RecognitionException {
		try {
			int _type = T__79;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:20:7: ( '<=' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:20:9: '<='
			{
			match("<="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__79"

	// $ANTLR start "T__80"
	public final void mT__80() throws RecognitionException {
		try {
			int _type = T__80;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:21:7: ( '=' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:21:9: '='
			{
			match('='); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__80"

	// $ANTLR start "T__81"
	public final void mT__81() throws RecognitionException {
		try {
			int _type = T__81;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:22:7: ( '==' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:22:9: '=='
			{
			match("=="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__81"

	// $ANTLR start "T__82"
	public final void mT__82() throws RecognitionException {
		try {
			int _type = T__82;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:23:7: ( '>' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:23:9: '>'
			{
			match('>'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__82"

	// $ANTLR start "T__83"
	public final void mT__83() throws RecognitionException {
		try {
			int _type = T__83;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:24:7: ( '>=' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:24:9: '>='
			{
			match(">="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__83"

	// $ANTLR start "T__84"
	public final void mT__84() throws RecognitionException {
		try {
			int _type = T__84;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:25:7: ( '[' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:25:9: '['
			{
			match('['); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__84"

	// $ANTLR start "T__85"
	public final void mT__85() throws RecognitionException {
		try {
			int _type = T__85;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:26:7: ( ']' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:26:9: ']'
			{
			match(']'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__85"

	// $ANTLR start "T__86"
	public final void mT__86() throws RecognitionException {
		try {
			int _type = T__86;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:27:7: ( 'boolean' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:27:9: 'boolean'
			{
			match("boolean"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__86"

	// $ANTLR start "T__87"
	public final void mT__87() throws RecognitionException {
		try {
			int _type = T__87;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:28:7: ( 'class' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:28:9: 'class'
			{
			match("class"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__87"

	// $ANTLR start "T__88"
	public final void mT__88() throws RecognitionException {
		try {
			int _type = T__88;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:29:7: ( 'else' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:29:9: 'else'
			{
			match("else"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__88"

	// $ANTLR start "T__89"
	public final void mT__89() throws RecognitionException {
		try {
			int _type = T__89;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:30:7: ( 'extends' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:30:9: 'extends'
			{
			match("extends"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__89"

	// $ANTLR start "T__90"
	public final void mT__90() throws RecognitionException {
		try {
			int _type = T__90;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:31:7: ( 'float' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:31:9: 'float'
			{
			match("float"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__90"

	// $ANTLR start "T__91"
	public final void mT__91() throws RecognitionException {
		try {
			int _type = T__91;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:32:7: ( 'if' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:32:9: 'if'
			{
			match("if"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__91"

	// $ANTLR start "T__92"
	public final void mT__92() throws RecognitionException {
		try {
			int _type = T__92;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:33:7: ( 'int' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:33:9: 'int'
			{
			match("int"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__92"

	// $ANTLR start "T__93"
	public final void mT__93() throws RecognitionException {
		try {
			int _type = T__93;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:34:7: ( 'new' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:34:9: 'new'
			{
			match("new"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__93"

	// $ANTLR start "T__94"
	public final void mT__94() throws RecognitionException {
		try {
			int _type = T__94;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:35:7: ( 'null' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:35:9: 'null'
			{
			match("null"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__94"

	// $ANTLR start "T__95"
	public final void mT__95() throws RecognitionException {
		try {
			int _type = T__95;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:36:7: ( 'read' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:36:9: 'read'
			{
			match("read"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__95"

	// $ANTLR start "T__96"
	public final void mT__96() throws RecognitionException {
		try {
			int _type = T__96;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:37:7: ( 'readf' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:37:9: 'readf'
			{
			match("readf"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__96"

	// $ANTLR start "T__97"
	public final void mT__97() throws RecognitionException {
		try {
			int _type = T__97;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:38:7: ( 'return' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:38:9: 'return'
			{
			match("return"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__97"

	// $ANTLR start "T__98"
	public final void mT__98() throws RecognitionException {
		try {
			int _type = T__98;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:39:7: ( 'this' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:39:9: 'this'
			{
			match("this"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__98"

	// $ANTLR start "T__99"
	public final void mT__99() throws RecognitionException {
		try {
			int _type = T__99;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:40:7: ( 'void' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:40:9: 'void'
			{
			match("void"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__99"

	// $ANTLR start "T__100"
	public final void mT__100() throws RecognitionException {
		try {
			int _type = T__100;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:41:8: ( 'while' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:41:10: 'while'
			{
			match("while"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__100"

	// $ANTLR start "T__101"
	public final void mT__101() throws RecognitionException {
		try {
			int _type = T__101;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:42:8: ( 'write' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:42:10: 'write'
			{
			match("write"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__101"

	// $ANTLR start "T__102"
	public final void mT__102() throws RecognitionException {
		try {
			int _type = T__102;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:43:8: ( 'writef' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:43:10: 'writef'
			{
			match("writef"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__102"

	// $ANTLR start "T__103"
	public final void mT__103() throws RecognitionException {
		try {
			int _type = T__103;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:44:8: ( 'writeln' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:44:10: 'writeln'
			{
			match("writeln"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__103"

	// $ANTLR start "T__104"
	public final void mT__104() throws RecognitionException {
		try {
			int _type = T__104;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:45:8: ( '{' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:45:10: '{'
			{
			match('{'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__104"

	// $ANTLR start "T__105"
	public final void mT__105() throws RecognitionException {
		try {
			int _type = T__105;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:46:8: ( '||' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:46:10: '||'
			{
			match("||"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__105"

	// $ANTLR start "T__106"
	public final void mT__106() throws RecognitionException {
		try {
			int _type = T__106;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:47:8: ( '}' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:47:10: '}'
			{
			match('}'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__106"

	// $ANTLR start "DecimalNumber"
	public final void mDecimalNumber() throws RecognitionException {
		try {
			int _type = DecimalNumber;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:581:2: ( '0' | '1' .. '9' ( '0' .. '9' )* )
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0=='0') ) {
				alt2=1;
			}
			else if ( ((LA2_0 >= '1' && LA2_0 <= '9')) ) {
				alt2=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}

			switch (alt2) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:581:4: '0'
					{
					match('0'); 
					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:582:4: '1' .. '9' ( '0' .. '9' )*
					{
					matchRange('1','9'); 
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:582:13: ( '0' .. '9' )*
					loop1:
					while (true) {
						int alt1=2;
						int LA1_0 = input.LA(1);
						if ( ((LA1_0 >= '0' && LA1_0 <= '9')) ) {
							alt1=1;
						}

						switch (alt1) {
						case 1 :
							// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
								input.consume();
							}
							else {
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							break loop1;
						}
					}

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DecimalNumber"

	// $ANTLR start "DigitNumber"
	public final void mDigitNumber() throws RecognitionException {
		try {
			int _type = DigitNumber;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:586:4: ( '0' .. '9' ( '0' .. '9' )* )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:586:6: '0' .. '9' ( '0' .. '9' )*
			{
			matchRange('0','9'); 
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:586:15: ( '0' .. '9' )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop3;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DigitNumber"

	// $ANTLR start "FloatNumber"
	public final void mFloatNumber() throws RecognitionException {
		try {
			int _type = FloatNumber;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:590:4: ( DecimalNumber '.' DigitNumber )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:590:6: DecimalNumber '.' DigitNumber
			{
			mDecimalNumber(); 

			match('.'); 
			mDigitNumber(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FloatNumber"

	// $ANTLR start "HexNumber"
	public final void mHexNumber() throws RecognitionException {
		try {
			int _type = HexNumber;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:594:2: ( HexPrefix ( HexDigit )+ )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:594:4: HexPrefix ( HexDigit )+
			{
			mHexPrefix(); 

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:594:14: ( HexDigit )+
			int cnt4=0;
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'F')||(LA4_0 >= 'a' && LA4_0 <= 'f')) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt4 >= 1 ) break loop4;
					EarlyExitException eee = new EarlyExitException(4, input);
					throw eee;
				}
				cnt4++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HexNumber"

	// $ANTLR start "HexPrefix"
	public final void mHexPrefix() throws RecognitionException {
		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:600:5: ( '0x' | '0X' )
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0=='0') ) {
				int LA5_1 = input.LA(2);
				if ( (LA5_1=='x') ) {
					alt5=1;
				}
				else if ( (LA5_1=='X') ) {
					alt5=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 5, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}

			switch (alt5) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:600:7: '0x'
					{
					match("0x"); 

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:600:14: '0X'
					{
					match("0X"); 

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HexPrefix"

	// $ANTLR start "HexDigit"
	public final void mHexDigit() throws RecognitionException {
		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:605:5: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HexDigit"

	// $ANTLR start "BooleanLiteral"
	public final void mBooleanLiteral() throws RecognitionException {
		try {
			int _type = BooleanLiteral;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:608:2: ( 'true' | 'false' )
			int alt6=2;
			int LA6_0 = input.LA(1);
			if ( (LA6_0=='t') ) {
				alt6=1;
			}
			else if ( (LA6_0=='f') ) {
				alt6=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 6, 0, input);
				throw nvae;
			}

			switch (alt6) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:608:4: 'true'
					{
					match("true"); 

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:609:4: 'false'
					{
					match("false"); 

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BooleanLiteral"

	// $ANTLR start "Identifier"
	public final void mIdentifier() throws RecognitionException {
		try {
			int _type = Identifier;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:614:2: ( Letter ( Letter | JavaIDDigit )* )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:614:4: Letter ( Letter | JavaIDDigit )*
			{
			mLetter(); 

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:614:11: ( Letter | JavaIDDigit )*
			loop7:
			while (true) {
				int alt7=2;
				int LA7_0 = input.LA(1);
				if ( (LA7_0=='$'||(LA7_0 >= '0' && LA7_0 <= '9')||(LA7_0 >= 'A' && LA7_0 <= 'Z')||LA7_0=='_'||(LA7_0 >= 'a' && LA7_0 <= 'z')||(LA7_0 >= '\u00C0' && LA7_0 <= '\u00D6')||(LA7_0 >= '\u00D8' && LA7_0 <= '\u00F6')||(LA7_0 >= '\u00F8' && LA7_0 <= '\u1FFF')||(LA7_0 >= '\u3040' && LA7_0 <= '\u318F')||(LA7_0 >= '\u3300' && LA7_0 <= '\u337F')||(LA7_0 >= '\u3400' && LA7_0 <= '\u3D2D')||(LA7_0 >= '\u4E00' && LA7_0 <= '\u9FFF')||(LA7_0 >= '\uF900' && LA7_0 <= '\uFAFF')) ) {
					alt7=1;
				}

				switch (alt7) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:
					{
					if ( input.LA(1)=='$'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop7;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Identifier"

	// $ANTLR start "Letter"
	public final void mLetter() throws RecognitionException {
		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:620:2: ( '\\u0024' | '\\u0041' .. '\\u005a' | '\\u005f' | '\\u0061' .. '\\u007a' | '\\u00c0' .. '\\u00d6' | '\\u00d8' .. '\\u00f6' | '\\u00f8' .. '\\u00ff' | '\\u0100' .. '\\u1fff' | '\\u3040' .. '\\u318f' | '\\u3300' .. '\\u337f' | '\\u3400' .. '\\u3d2d' | '\\u4e00' .. '\\u9fff' | '\\uf900' .. '\\ufaff' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:
			{
			if ( input.LA(1)=='$'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Letter"

	// $ANTLR start "JavaIDDigit"
	public final void mJavaIDDigit() throws RecognitionException {
		try {
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:637:2: ( '\\u0030' .. '\\u0039' | '\\u0660' .. '\\u0669' | '\\u06f0' .. '\\u06f9' | '\\u0966' .. '\\u096f' | '\\u09e6' .. '\\u09ef' | '\\u0a66' .. '\\u0a6f' | '\\u0ae6' .. '\\u0aef' | '\\u0b66' .. '\\u0b6f' | '\\u0be7' .. '\\u0bef' | '\\u0c66' .. '\\u0c6f' | '\\u0ce6' .. '\\u0cef' | '\\u0d66' .. '\\u0d6f' | '\\u0e50' .. '\\u0e59' | '\\u0ed0' .. '\\u0ed9' | '\\u1040' .. '\\u1049' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= '\u0660' && input.LA(1) <= '\u0669')||(input.LA(1) >= '\u06F0' && input.LA(1) <= '\u06F9')||(input.LA(1) >= '\u0966' && input.LA(1) <= '\u096F')||(input.LA(1) >= '\u09E6' && input.LA(1) <= '\u09EF')||(input.LA(1) >= '\u0A66' && input.LA(1) <= '\u0A6F')||(input.LA(1) >= '\u0AE6' && input.LA(1) <= '\u0AEF')||(input.LA(1) >= '\u0B66' && input.LA(1) <= '\u0B6F')||(input.LA(1) >= '\u0BE7' && input.LA(1) <= '\u0BEF')||(input.LA(1) >= '\u0C66' && input.LA(1) <= '\u0C6F')||(input.LA(1) >= '\u0CE6' && input.LA(1) <= '\u0CEF')||(input.LA(1) >= '\u0D66' && input.LA(1) <= '\u0D6F')||(input.LA(1) >= '\u0E50' && input.LA(1) <= '\u0E59')||(input.LA(1) >= '\u0ED0' && input.LA(1) <= '\u0ED9')||(input.LA(1) >= '\u1040' && input.LA(1) <= '\u1049') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "JavaIDDigit"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:654:2: ( ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' ) )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:654:4: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )
			{
			if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			_channel=HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:658:2: ( '/*' ( options {greedy=false; } : . )* '*/' )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:658:4: '/*' ( options {greedy=false; } : . )* '*/'
			{
			match("/*"); 

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:658:9: ( options {greedy=false; } : . )*
			loop8:
			while (true) {
				int alt8=2;
				int LA8_0 = input.LA(1);
				if ( (LA8_0=='*') ) {
					int LA8_1 = input.LA(2);
					if ( (LA8_1=='/') ) {
						alt8=2;
					}
					else if ( ((LA8_1 >= '\u0000' && LA8_1 <= '.')||(LA8_1 >= '0' && LA8_1 <= '\uFFFF')) ) {
						alt8=1;
					}

				}
				else if ( ((LA8_0 >= '\u0000' && LA8_0 <= ')')||(LA8_0 >= '+' && LA8_0 <= '\uFFFF')) ) {
					alt8=1;
				}

				switch (alt8) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:658:37: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop8;
				}
			}

			match("*/"); 

			_channel=HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT"

	// $ANTLR start "LINE_COMMENT"
	public final void mLINE_COMMENT() throws RecognitionException {
		try {
			int _type = LINE_COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:662:2: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r\\n' | '\\r' | '\\n' )? )
			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:662:4: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r\\n' | '\\r' | '\\n' )?
			{
			match("//"); 

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:662:9: (~ ( '\\n' | '\\r' ) )*
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( ((LA9_0 >= '\u0000' && LA9_0 <= '\t')||(LA9_0 >= '\u000B' && LA9_0 <= '\f')||(LA9_0 >= '\u000E' && LA9_0 <= '\uFFFF')) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop9;
				}
			}

			// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:662:24: ( '\\r\\n' | '\\r' | '\\n' )?
			int alt10=4;
			int LA10_0 = input.LA(1);
			if ( (LA10_0=='\r') ) {
				int LA10_1 = input.LA(2);
				if ( (LA10_1=='\n') ) {
					alt10=1;
				}
			}
			else if ( (LA10_0=='\n') ) {
				alt10=3;
			}
			switch (alt10) {
				case 1 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:662:25: '\\r\\n'
					{
					match("\r\n"); 

					}
					break;
				case 2 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:662:34: '\\r'
					{
					match('\r'); 
					}
					break;
				case 3 :
					// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:662:41: '\\n'
					{
					match('\n'); 
					}
					break;

			}

			_channel=HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LINE_COMMENT"

	@Override
	public void mTokens() throws RecognitionException {
		// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:8: ( T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | T__93 | T__94 | T__95 | T__96 | T__97 | T__98 | T__99 | T__100 | T__101 | T__102 | T__103 | T__104 | T__105 | T__106 | DecimalNumber | DigitNumber | FloatNumber | HexNumber | BooleanLiteral | Identifier | WS | COMMENT | LINE_COMMENT )
		int alt11=51;
		alt11 = dfa11.predict(input);
		switch (alt11) {
			case 1 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:10: T__65
				{
				mT__65(); 

				}
				break;
			case 2 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:16: T__66
				{
				mT__66(); 

				}
				break;
			case 3 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:22: T__67
				{
				mT__67(); 

				}
				break;
			case 4 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:28: T__68
				{
				mT__68(); 

				}
				break;
			case 5 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:34: T__69
				{
				mT__69(); 

				}
				break;
			case 6 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:40: T__70
				{
				mT__70(); 

				}
				break;
			case 7 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:46: T__71
				{
				mT__71(); 

				}
				break;
			case 8 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:52: T__72
				{
				mT__72(); 

				}
				break;
			case 9 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:58: T__73
				{
				mT__73(); 

				}
				break;
			case 10 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:64: T__74
				{
				mT__74(); 

				}
				break;
			case 11 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:70: T__75
				{
				mT__75(); 

				}
				break;
			case 12 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:76: T__76
				{
				mT__76(); 

				}
				break;
			case 13 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:82: T__77
				{
				mT__77(); 

				}
				break;
			case 14 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:88: T__78
				{
				mT__78(); 

				}
				break;
			case 15 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:94: T__79
				{
				mT__79(); 

				}
				break;
			case 16 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:100: T__80
				{
				mT__80(); 

				}
				break;
			case 17 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:106: T__81
				{
				mT__81(); 

				}
				break;
			case 18 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:112: T__82
				{
				mT__82(); 

				}
				break;
			case 19 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:118: T__83
				{
				mT__83(); 

				}
				break;
			case 20 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:124: T__84
				{
				mT__84(); 

				}
				break;
			case 21 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:130: T__85
				{
				mT__85(); 

				}
				break;
			case 22 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:136: T__86
				{
				mT__86(); 

				}
				break;
			case 23 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:142: T__87
				{
				mT__87(); 

				}
				break;
			case 24 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:148: T__88
				{
				mT__88(); 

				}
				break;
			case 25 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:154: T__89
				{
				mT__89(); 

				}
				break;
			case 26 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:160: T__90
				{
				mT__90(); 

				}
				break;
			case 27 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:166: T__91
				{
				mT__91(); 

				}
				break;
			case 28 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:172: T__92
				{
				mT__92(); 

				}
				break;
			case 29 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:178: T__93
				{
				mT__93(); 

				}
				break;
			case 30 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:184: T__94
				{
				mT__94(); 

				}
				break;
			case 31 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:190: T__95
				{
				mT__95(); 

				}
				break;
			case 32 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:196: T__96
				{
				mT__96(); 

				}
				break;
			case 33 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:202: T__97
				{
				mT__97(); 

				}
				break;
			case 34 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:208: T__98
				{
				mT__98(); 

				}
				break;
			case 35 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:214: T__99
				{
				mT__99(); 

				}
				break;
			case 36 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:220: T__100
				{
				mT__100(); 

				}
				break;
			case 37 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:227: T__101
				{
				mT__101(); 

				}
				break;
			case 38 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:234: T__102
				{
				mT__102(); 

				}
				break;
			case 39 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:241: T__103
				{
				mT__103(); 

				}
				break;
			case 40 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:248: T__104
				{
				mT__104(); 

				}
				break;
			case 41 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:255: T__105
				{
				mT__105(); 

				}
				break;
			case 42 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:262: T__106
				{
				mT__106(); 

				}
				break;
			case 43 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:269: DecimalNumber
				{
				mDecimalNumber(); 

				}
				break;
			case 44 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:283: DigitNumber
				{
				mDigitNumber(); 

				}
				break;
			case 45 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:295: FloatNumber
				{
				mFloatNumber(); 

				}
				break;
			case 46 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:307: HexNumber
				{
				mHexNumber(); 

				}
				break;
			case 47 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:317: BooleanLiteral
				{
				mBooleanLiteral(); 

				}
				break;
			case 48 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:332: Identifier
				{
				mIdentifier(); 

				}
				break;
			case 49 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:343: WS
				{
				mWS(); 

				}
				break;
			case 50 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:346: COMMENT
				{
				mCOMMENT(); 

				}
				break;
			case 51 :
				// /home/luca/svn-repos/cd_students/2015ss/master/CD2_A1/src/cd/parser/Javali.g:1:354: LINE_COMMENT
				{
				mLINE_COMMENT(); 

				}
				break;

		}
	}


	protected DFA11 dfa11 = new DFA11(this);
	static final String DFA11_eotS =
		"\1\uffff\1\44\11\uffff\1\47\1\uffff\1\51\1\53\1\55\2\uffff\12\41\3\uffff"+
		"\2\77\15\uffff\6\41\1\111\11\41\4\uffff\1\77\6\41\1\uffff\1\132\1\133"+
		"\12\41\1\146\3\41\2\uffff\1\152\1\154\1\41\1\156\1\157\1\160\3\41\1\164"+
		"\1\uffff\1\41\1\166\1\157\1\uffff\1\167\1\uffff\1\41\3\uffff\1\171\1\174"+
		"\1\41\1\uffff\1\41\2\uffff\1\177\1\uffff\1\u0080\1\41\1\uffff\1\u0082"+
		"\1\u0083\2\uffff\1\u0084\3\uffff";
	static final String DFA11_eofS =
		"\u0085\uffff";
	static final String DFA11_minS =
		"\1\11\1\75\11\uffff\1\52\1\uffff\3\75\2\uffff\1\157\2\154\1\141\1\146"+
		"\2\145\1\150\1\157\1\150\3\uffff\2\56\15\uffff\1\157\1\141\1\163\1\164"+
		"\1\157\1\154\1\44\1\164\1\167\1\154\1\141\1\151\1\165\3\151\4\uffff\1"+
		"\56\1\154\1\163\2\145\1\141\1\163\1\uffff\2\44\1\154\1\144\1\165\1\163"+
		"\1\145\1\144\1\154\1\164\1\145\1\163\1\44\1\156\1\164\1\145\2\uffff\2"+
		"\44\1\162\3\44\2\145\1\141\1\44\1\uffff\1\144\2\44\1\uffff\1\44\1\uffff"+
		"\1\156\3\uffff\2\44\1\156\1\uffff\1\163\2\uffff\1\44\1\uffff\1\44\1\156"+
		"\1\uffff\2\44\2\uffff\1\44\3\uffff";
	static final String DFA11_maxS =
		"\1\ufaff\1\75\11\uffff\1\57\1\uffff\3\75\2\uffff\1\157\1\154\1\170\1\154"+
		"\1\156\1\165\1\145\1\162\1\157\1\162\3\uffff\1\170\1\71\15\uffff\1\157"+
		"\1\141\1\163\1\164\1\157\1\154\1\ufaff\1\164\1\167\1\154\1\164\1\151\1"+
		"\165\3\151\4\uffff\1\71\1\154\1\163\2\145\1\141\1\163\1\uffff\2\ufaff"+
		"\1\154\1\144\1\165\1\163\1\145\1\144\1\154\1\164\1\145\1\163\1\ufaff\1"+
		"\156\1\164\1\145\2\uffff\2\ufaff\1\162\3\ufaff\2\145\1\141\1\ufaff\1\uffff"+
		"\1\144\2\ufaff\1\uffff\1\ufaff\1\uffff\1\156\3\uffff\2\ufaff\1\156\1\uffff"+
		"\1\163\2\uffff\1\ufaff\1\uffff\1\ufaff\1\156\1\uffff\2\ufaff\2\uffff\1"+
		"\ufaff\3\uffff";
	static final String DFA11_acceptS =
		"\2\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\uffff\1\15\3\uffff"+
		"\1\24\1\25\12\uffff\1\50\1\51\1\52\2\uffff\1\60\1\61\1\2\1\1\1\62\1\63"+
		"\1\14\1\17\1\16\1\21\1\20\1\23\1\22\20\uffff\1\56\1\53\1\54\1\55\7\uffff"+
		"\1\33\20\uffff\1\34\1\35\12\uffff\1\30\3\uffff\1\36\1\uffff\1\37\1\uffff"+
		"\1\42\1\57\1\43\3\uffff\1\27\1\uffff\1\32\1\40\1\uffff\1\44\2\uffff\1"+
		"\45\2\uffff\1\41\1\46\1\uffff\1\26\1\31\1\47";
	static final String DFA11_specialS =
		"\u0085\uffff}>";
	static final String[] DFA11_transitionS = {
			"\2\42\1\uffff\2\42\22\uffff\1\42\1\1\2\uffff\1\41\1\2\1\3\1\uffff\1\4"+
			"\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\37\11\40\1\uffff\1\14\1\15\1\16\1"+
			"\17\2\uffff\32\41\1\20\1\uffff\1\21\1\uffff\1\41\1\uffff\1\41\1\22\1"+
			"\23\1\41\1\24\1\25\2\41\1\26\4\41\1\27\3\41\1\30\1\41\1\31\1\41\1\32"+
			"\1\33\3\41\1\34\1\35\1\36\102\uffff\27\41\1\uffff\37\41\1\uffff\u1f08"+
			"\41\u1040\uffff\u0150\41\u0170\uffff\u0080\41\u0080\uffff\u092e\41\u10d2"+
			"\uffff\u5200\41\u5900\uffff\u0200\41",
			"\1\43",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\45\4\uffff\1\46",
			"",
			"\1\50",
			"\1\52",
			"\1\54",
			"",
			"",
			"\1\56",
			"\1\57",
			"\1\60\13\uffff\1\61",
			"\1\63\12\uffff\1\62",
			"\1\64\7\uffff\1\65",
			"\1\66\17\uffff\1\67",
			"\1\70",
			"\1\71\11\uffff\1\72",
			"\1\73",
			"\1\74\11\uffff\1\75",
			"",
			"",
			"",
			"\1\101\1\uffff\12\100\36\uffff\1\76\37\uffff\1\76",
			"\1\101\1\uffff\12\102",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\103",
			"\1\104",
			"\1\105",
			"\1\106",
			"\1\107",
			"\1\110",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\112",
			"\1\113",
			"\1\114",
			"\1\115\22\uffff\1\116",
			"\1\117",
			"\1\120",
			"\1\121",
			"\1\122",
			"\1\123",
			"",
			"",
			"",
			"",
			"\1\101\1\uffff\12\102",
			"\1\124",
			"\1\125",
			"\1\126",
			"\1\127",
			"\1\130",
			"\1\131",
			"",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\134",
			"\1\135",
			"\1\136",
			"\1\137",
			"\1\140",
			"\1\141",
			"\1\142",
			"\1\143",
			"\1\144",
			"\1\145",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\147",
			"\1\150",
			"\1\151",
			"",
			"",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\5\41\1\153\24"+
			"\41\105\uffff\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150"+
			"\41\u0170\uffff\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900"+
			"\uffff\u0200\41",
			"\1\155",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\161",
			"\1\162",
			"\1\163",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"",
			"\1\165",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"",
			"\1\170",
			"",
			"",
			"",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\5\41\1\172\5"+
			"\41\1\173\16\41\105\uffff\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040"+
			"\uffff\u0150\41\u0170\uffff\u0080\41\u0080\uffff\u092e\41\u10d2\uffff"+
			"\u5200\41\u5900\uffff\u0200\41",
			"\1\175",
			"",
			"\1\176",
			"",
			"",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\u0081",
			"",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"",
			"",
			"\1\41\13\uffff\12\41\7\uffff\32\41\4\uffff\1\41\1\uffff\32\41\105\uffff"+
			"\27\41\1\uffff\37\41\1\uffff\u1f08\41\u1040\uffff\u0150\41\u0170\uffff"+
			"\u0080\41\u0080\uffff\u092e\41\u10d2\uffff\u5200\41\u5900\uffff\u0200"+
			"\41",
			"",
			"",
			""
	};

	static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
	static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
	static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
	static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
	static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
	static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
	static final short[][] DFA11_transition;

	static {
		int numStates = DFA11_transitionS.length;
		DFA11_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
		}
	}

	protected class DFA11 extends DFA {

		public DFA11(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 11;
			this.eot = DFA11_eot;
			this.eof = DFA11_eof;
			this.min = DFA11_min;
			this.max = DFA11_max;
			this.accept = DFA11_accept;
			this.special = DFA11_special;
			this.transition = DFA11_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | T__93 | T__94 | T__95 | T__96 | T__97 | T__98 | T__99 | T__100 | T__101 | T__102 | T__103 | T__104 | T__105 | T__106 | DecimalNumber | DigitNumber | FloatNumber | HexNumber | BooleanLiteral | Identifier | WS | COMMENT | LINE_COMMENT );";
		}
	}

}
