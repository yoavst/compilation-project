/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/*************/
/* USER CODE */
/*************/
import java_cup.runtime.*;

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/
   
/*****************************************************/ 
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/ 
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column

/*******************************************************************************/
/* Note that this has to be the EXACT same name of the class the CUP generates */
/*******************************************************************************/
%cupsym TokenNames

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup

/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/   
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */  
/*****************************************************************************/   
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getLine() { return yyline + 1; } 

	/**********************************************/
	/* Enable token position extraction from main */
	/**********************************************/
	public int getTokenStartPosition() { return yycolumn + 1; } 
	
	private Integer parseOrThrow(String num) {
		int parsed = Integer.parseInt(num);
		if (parsed < -32768 || parsed > 32767) 
			throw new UnsupportedOperationException("Invalid integer: " + num);
		return parsed;
	}

	private String parseString(String text) {
		return text.substring(1, text.length() - 1);
	}
%}


/***********************/
/* MACRO DECALARATIONS */
/***********************/
LineTerminator		= \r|\n|\r\n
WhiteSpace			= {LineTerminator} | [ \t\f]
INTEGER				= 0 | -?[1-9][0-9]*
ID					= [a-zA-Z][a-zA-Z0-9]*
STRING				= "\"" [a-z|A-Z]* "\""
COMMENT_CHAR		= [a-zA-Z0-9 \t\f\(\)\[\]\{\}?!+\-*\/.;]
SINGLE_LINE_COMMENT = "//" {COMMENT_CHAR}*
MULTI_LINE_COMMENT  = "/*" ({COMMENT_CHAR} | LineTerminator)* ~"*/"

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/
   
/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {
/* keywords */
"class"				{ return symbol(TokenNames.CLASS); }
"array"				{ return symbol(TokenNames.ARRAY); }
"while"				{ return symbol(TokenNames.WHILE); }
"extends"			{ return symbol(TokenNames.EXTENDS); }
"return"			{ return symbol(TokenNames.RETURN); }
"new"				{ return symbol(TokenNames.NEW); }
"if"				{ return symbol(TokenNames.IF); }

/* null literal */
"nil"				{ return symbol(TokenNames.NIL); }

/* separators */
"("					{ return symbol(TokenNames.LPAREN); }
")"					{ return symbol(TokenNames.RPAREN); }
"["					{ return symbol(TokenNames.LBRACK); }
"]"					{ return symbol(TokenNames.RBRACK); }
"{"					{ return symbol(TokenNames.LBRACE); }
"}"					{ return symbol(TokenNames.RBRACE); }
","					{ return symbol(TokenNames.COMMA); }
";"					{ return symbol(TokenNames.SEMICOLON); }
"."					{ return symbol(TokenNames.DOT); }

/* operators */
"="					{ return symbol(TokenNames.EQ); }
"<"					{ return symbol(TokenNames.LT); }
"+"					{ return symbol(TokenNames.PLUS); }
"-"					{ return symbol(TokenNames.MINUS); }
"*"					{ return symbol(TokenNames.TIMES); }
"/"					{ return symbol(TokenNames.DIVIDE); }
":="				{ return symbol(TokenNames.ASSIGN); }

{ID}				{ return symbol(TokenNames.ID, new String(yytext())); }
{INTEGER}			{ return symbol(TokenNames.INT, parseOrThrow(yytext())); }
{STRING}			{ return symbol(TokenNames.STRING, parseString(yytext())); }
{WhiteSpace}		{ /* just skip what was found, do nothing */ }
{LineTerminator}	{ /* just skip what was found, do nothing */ }
{SINGLE_LINE_COMMENT} { /* just skip what was found, do nothing */ }
{MULTI_LINE_COMMENT} { /* just skip what was found, do nothing */ } 
<<EOF>>				{ return symbol(TokenNames.EOF); }
[^] 				{ throw new UnsupportedOperationException("Illegal character <"+yytext()+">"); }
}
