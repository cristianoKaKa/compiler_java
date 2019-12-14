
import java.util.*;
import java.io.*;

enum Token_type{
    ORIGIN,SCALE,ROT,IS,TO,STEP,DRAW,FOR,FROM,
    T,
    SEMICO,L_BRACKET,R_BRACKET,COMMA,
    PLUS,MINUS,MUL,DIV,POWER,
    FUNC,
    CONST_ID,
    NONTOKEN,
    ERRTOKEN
}

enum FuncName{
    sin,cos,tan,log,exp,sqrt,NULL
}

class Token{
    Token_type type;
    String lexeme;
    double value;
    FuncName funcName;
    // 缺省函数指针的替代（maybe匿名内部类）
    Token(){
        this.value=0.0;
        this.funcName=FuncName.NULL;
        this.type = Token_type.ERRTOKEN;
        this.lexeme="";
    }
    Token(Token_type type,String lexeme,double value,FuncName funcName){
        this.funcName=funcName;
        this.type=type;
        this.lexeme=lexeme;
        this.value=value;
    }
    public void setType(Token_type type){
        this.type = type;
    }
    public void setFuncName(FuncName funcName) {
        this.funcName = funcName;
    }
    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }
    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
    public FuncName getFuncName() {
        return funcName;
    }
    public String getLexeme() {
        return lexeme;
    }
    public Token_type getType() {
        return type;
    }
}

public class Lexical<TOKEN_LEN> {

    //初始化文件路径
    final static String FILE_PATH = "src/com/hit/demo/test.txt";
    //定义行数
    int LineNo = 0;

    boolean isAlpha(int a){
        if ((a>=65&&a<=90)||(a>=97&&a<=122)) return true;
        else return false;
    }
    boolean isNum(int a){
        if(a>=48&&a<=57) return true;
        else return false;
    }

    Token[] TokenTab=new Token[]{
            new Token(Token_type.CONST_ID,"PI",3.1415926,FuncName.NULL),
            new Token(Token_type.CONST_ID,"E",2.71828,FuncName.NULL),
            new Token(Token_type.T,"T",0.0,FuncName.NULL),
            new Token(Token_type.FUNC,"SIN",0.0,FuncName.sin),
            new Token(Token_type.FUNC,"COS",0.0,FuncName.cos),
            new Token(Token_type.FUNC,"TAN",0.0,FuncName.tan),
            new Token(Token_type.FUNC,"LN",0.0,FuncName.log),
            new Token(Token_type.FUNC,"EXP",0.0,FuncName.exp),
            new Token(Token_type.FUNC,"SQRT",0.0,FuncName.sqrt),
            new Token(Token_type.ORIGIN,"ORIGIN",0.0,FuncName.NULL),
            new Token(Token_type.SCALE,"SCALE",0.0,FuncName.NULL),
            new Token(Token_type.ROT,"ROT",0.0,FuncName.NULL),
            new Token(Token_type.IS,"IS",0.0,FuncName.NULL),
            new Token(Token_type.FOR,"FOR",0.0,FuncName.NULL),
            new Token(Token_type.FROM,"FROM",0.0,FuncName.NULL),
            new Token(Token_type.TO,"TO",0.0,FuncName.NULL),
            new Token(Token_type.STEP,"STEP",0.0,FuncName.NULL),
            new Token(Token_type.DRAW,"DRAW",0.0,FuncName.NULL)
    };



    public String getFile_path(){
        return FILE_PATH;
    }

    private ArrayList<Character> TokenBuffer = new ArrayList<>();          //初始化字符缓冲区

    public InputStream InitScanner(String FileName) throws FileNotFoundException {  //初始化此法分析器
        LineNo = 1;
        InputStream inputStream = new FileInputStream(new File(FileName));
        PushbackInputStream is = new PushbackInputStream(inputStream);
        return is;
    }

    public void CloseScanner(PushbackInputStream is) throws IOException {          //关闭词法分析器
        is.close();
    }

    /*
    *   读取字符、回退字符使用javaIO的read()和unread(int)来实现
    * */
    //加入记号到缓冲区
    private void AddCharTokenString(char ch){
        //ch为大写字母
        int len = TokenBuffer.size();
        TokenBuffer.add(len, ch);
        //System.out.println(" "+TokenBuffer[len]);
    }
    //清空缓冲区
    void EmptyTokenBuffer(){                                            //清空缓冲区
        TokenBuffer.clear();
    }
    //判断所给字符串是否在符号表中
    Token JudgeKeyToken(ArrayList<Character> IDString){
        //将ArrayList<Character>类型转为String类
        StringBuilder sb = new StringBuilder();
        for (Character ch :IDString){
            sb.append(ch);
        }
        String str = sb.toString();
        for(int i=0;i<TokenTab.length;i++){
            if (str.equals(TokenTab[i].lexeme)){
                return TokenTab[i];
            }
        }
        return new Token(Token_type.ERRTOKEN, str,0.0,FuncName.NULL);
    }
    //获取一个记号
    public Token GetToken(PushbackInputStream is) throws IOException {
        Token token = new Token();
        //asciiChar存放read()返回的ascii码值
        int asciiChar;
        EmptyTokenBuffer();
        while (true){
            asciiChar = is.read();
            if (asciiChar==-1){                     //文件结束
                token.setType(Token_type.NONTOKEN);
                return token;
            }
            if(asciiChar==10){                      //行数加一
                LineNo++;
            }
            if(asciiChar!=' '&&asciiChar!=13&&asciiChar!=10) break;                //跳过空格或归位符或换行符
        }
        AddCharTokenString(Character.toUpperCase((char)asciiChar));        //若不是空格、回车、文件结束符，则添加到大写字符缓冲区中
        if(isAlpha(asciiChar)){
            while (true){
                asciiChar = is.read();      //循环读取下一字符，直到该字符不是数字或字母
                if(isNum(asciiChar)||isAlpha(asciiChar)) {
                    AddCharTokenString(Character.toUpperCase((char)asciiChar));
                }
                else break;
            }
            is.unread(asciiChar);               //回退asciiChar字符到输入流中
            token = JudgeKeyToken(TokenBuffer);
            return token;
        }
        else if(isNum(asciiChar)){
            while (true){
                asciiChar = is.read();
                if (isNum(asciiChar)) {
                    AddCharTokenString((char) asciiChar);
                }
                else break;
            }
            //匹配小数点
            if(asciiChar==46){
                AddCharTokenString((char)asciiChar);
                while (true){
                    asciiChar = is.read();
                    if(isNum(asciiChar)){
                        AddCharTokenString((char)asciiChar);
                    }
                    else break;
                }
            }
            is.unread(asciiChar);
            StringBuilder sb = new StringBuilder();
            for (Character ch :TokenBuffer){
                sb.append(ch);
            }
            String str = sb.toString();
            double a=Double.parseDouble(str);
            token.setLexeme(str);
            token.setValue(a);
            token.setType(Token_type.CONST_ID);
            return token;
        }
        else{                                                           //输入字符为运算符或分隔符时
            switch (asciiChar){
                case ',' : token.setType(Token_type.COMMA);token.setLexeme(",");   break;
                case '(' : token.setType(Token_type.L_BRACKET); token.setLexeme("(");break;
                case ')' : token.setType(Token_type.R_BRACKET);token.setLexeme(")"); break;
                case ';' : token.setType(Token_type.SEMICO); token.setLexeme(";");   break;
                case '+' : token.setType(Token_type.PLUS);   token.setLexeme("+");   break;
                case '-' :
                    asciiChar = is.read();
                    if (asciiChar=='-'){                            //匹配到 “--” 即注释时
                        while (asciiChar!='\n'&&asciiChar!=-1){
                            asciiChar = is.read();
                        }
                        is.unread(asciiChar);
                        return GetToken(is);                        //检测到注释后，略过并递归调用GetToken检测下一记号
                    }
                    else {
                        is.unread(asciiChar);
                        token.setType(Token_type.MINUS);
                        token.setLexeme("-");
                        break;
                    }
                case '/':
                    asciiChar = is.read();
                    if(asciiChar=='/'){
                        while (asciiChar!='\n'&&asciiChar!=-1){
                            asciiChar = is.read();
                        }
                        is.unread(asciiChar);
                        return GetToken(is);
                    }
                    else {
                        is.unread(asciiChar);
                        token.setType(Token_type.DIV);
                        token.setLexeme("/");
                        break;
                    }
                case '*':
                    asciiChar = is.read();
                    if (asciiChar=='*'){            //乘方
                        token.setType(Token_type.POWER);
                        token.setLexeme("**");
                        break;
                    }
                    else {
                        is.unread(asciiChar);
                        token.setType(Token_type.MUL);
                        token.setLexeme("*");
                        break;
                    }
                default: token.setType(Token_type.ERRTOKEN);
            }
        }
        return token;
    }

    public static void main(String[] args) throws IOException {
        Lexical lexical = new Lexical();
        try{
            int count;
            InputStream is= lexical.InitScanner(lexical.getFile_path());
            PushbackInputStream pis = new PushbackInputStream(is);
            Token token;
            System.out.println("______________________________________________");
            System.out.println("记号类别        字符串        常数值      函数");
            while (true){
                token = lexical.GetToken(pis);
                if(token.getType()!=Token_type.NONTOKEN){
                    System.out.println(token.getType()+"        "+token.getLexeme()+"         "+token.getValue()+"         "+token.getFuncName());
                }
                else break;
            }
            System.out.println("______________________________________________");
            //System.out.println(lexical.LineNo);
            lexical.CloseScanner(pis);
        }catch (IOException E){
            E.printStackTrace();
        }
    }
}
