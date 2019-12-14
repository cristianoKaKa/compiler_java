

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

class Paramater{
    Double value;
    Paramater(Double d){
        value = d;
    }
}

//表达式的语法树节点类
class ExprNode{
    public Token_type Opcode;
}
//派生二元运算节点
class OperatorNode extends ExprNode{
    ExprNode Left;
    ExprNode Right;
}
//派生函数节点
class FuncNode extends ExprNode{
    ExprNode Child;
    FuncName Func;
}
//派生常数叶子节点
class ConstNode extends ExprNode{
    double Const;
}
//派生参数T叶子节点
class ParmNode extends ExprNode{
    Paramater Parm ;
}

public class Parser {
    //初始化全局变量
    public static Paramater paramater = new Paramater(0.0);
    public static double Origin_x=0.0, Origin_y=0.0;
    public static double Rot_ang = 0.0;
    public static double Scale_x=1, Scale_y=1;

    //创建语义分析类对象
    public Semantic semantic = new Semantic();

    static Lexical lexical = new Lexical();
    static Token token;
    //初始化输入流
    static InputStream is;
    static {
        try {
            is = lexical.InitScanner(lexical.getFile_path());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    static PushbackInputStream pis = new PushbackInputStream(is);

    //创建语法树节点
    ExprNode MakeExprNode(Token_type opcode,Object...args){
        ExprNode exprNode;
        switch (opcode){
            case CONST_ID:                              //常数
                //ConstNode exprNode = new ConstNode();
                exprNode = new ConstNode();
                exprNode.Opcode = opcode;
                ((ConstNode) exprNode).Const = Double.valueOf(args[0].toString());
                break;
            case T:                                      //参数
                exprNode = new ParmNode();
                exprNode.Opcode = opcode;
                ((ParmNode) exprNode).Parm = Parser.paramater;
                //System.out.println("Parm = "+((ParmNode)exprNode).Parm.value);
                break;
            case FUNC:                                  //函数
                exprNode = new FuncNode();
                exprNode.Opcode = opcode;
                ((FuncNode) exprNode).Func = (FuncName) args[0];
                ((FuncNode) exprNode).Child = (ExprNode) args[1];
                break;
            default:                                    //二元运算节点
                exprNode = new OperatorNode();
                exprNode.Opcode = opcode;
                ((OperatorNode) exprNode).Left = (ExprNode) args[0];
                ((OperatorNode) exprNode).Right = (ExprNode) args[1];
                break;
        }
        return exprNode;
    }
    //先序遍历打印语法树
    public void PrintSyntaxTree(ExprNode root,int indent){
        for (int i=0;i<indent;i++){
            System.out.print(" ");
        }
        //打印根节点
        switch (root.Opcode){
            case PLUS:  System.out.println("+");break;
            case MINUS: System.out.println("-");break;
            case MUL:   System.out.println("*");break;
            case DIV:   System.out.println("/");break;
            case POWER: System.out.println("**");break;
            case FUNC:  System.out.println(((FuncNode)root).Func);break;
            case CONST_ID:  System.out.println(((ConstNode)root).Const);break;
            case T:     System.out.println("T");break;
            default:    System.out.println("Error Tree Node!");
        }
        //叶子节点返回
        if (root.Opcode==Token_type.CONST_ID || root.Opcode==Token_type.T){
            return;
        }
        //函数递归打印子节点
        else if (root.Opcode==Token_type.FUNC){
            PrintSyntaxTree(((FuncNode)root).Child,indent+1);
        }
        //操作符递归打印两个孩子节点
        else {
            PrintSyntaxTree(((OperatorNode)root).Left,indent+1);
            PrintSyntaxTree(((OperatorNode)root).Right,indent+1);
        }
    }

    //处理错误
    public void SyntaxError(int type){
        if (type==1) System.out.println("Lexical Error!");
        else if (type==2) System.out.println("Parser Error!");
    }
    //FetchToken()调用GetToken()获取记号，并处理词法错误
    public void FetchToken() throws IOException {
        token = lexical.GetToken(pis);
        if (token.type == Token_type.ERRTOKEN) SyntaxError(1);
    }
    //MatchToken()匹配当前记号，若成功则调用FetchToken()匹配下一个，并处理语法错误
    public void MatchToken(Token_type the_token) throws IOException {
        if(token.type!=the_token) SyntaxError(2);
        FetchToken();
    }
    //打印match语句
    public void callMatch(String s){
        System.out.println("Match Token "+s);
    }

    //Program->{Statement SEMICO}
    public void Program() throws IOException {
        System.out.println("Enter in Program:");
        while (token.type!=Token_type.NONTOKEN){
            Statement();
            MatchToken(Token_type.SEMICO);  callMatch("SEMICO");
        }
        System.out.println("Exit from Program.");
    }
    //Statement->OriginStatement | ScaleStatement | RotStatement | ForStatement
    public void Statement() throws IOException {
        System.out.println("Enter in Statement:");
        switch (token.type){
            case ORIGIN:    OriginStatement(); break;
            case SCALE:     ScaleStatement();  break;
            case ROT:       RotStatement();    break;
            case FOR:       ForStatement();    break;
            default:        SyntaxError(2);
        }
        System.out.println("Exit from Statement.");
    }
    //OriginStatement->ORIGIN IS L_BRACKET Expression COMMA Expression R_BRACKET
    public void OriginStatement() throws IOException {
        ExprNode leftNode,rightNode;
        System.out.println("Enter in OriginStatement");
        MatchToken(Token_type.ORIGIN);  callMatch("ORIGIN");
        MatchToken(Token_type.IS);      callMatch("IS");
        MatchToken(Token_type.L_BRACKET);   callMatch("L_BRACKET");
        leftNode = Expression();
        Origin_x = semantic.GetExprValue(leftNode);
        MatchToken(Token_type.COMMA);   callMatch("COMMA");
        rightNode = Expression();
        Origin_y = semantic.GetExprValue(rightNode);
        MatchToken(Token_type.R_BRACKET);   callMatch("R_BRACKET");
        System.out.println("Exit from OriginStatement");
    }
    //ScaleStatement->SCALE IS L_BRACKET Expression COMMA Expression R_BRACKET
    public void ScaleStatement() throws IOException {
        ExprNode leftNode,rightNode;
        System.out.println("Enter in ScaleStatement:");
        MatchToken(Token_type.SCALE);   callMatch("SCALE");
        MatchToken(Token_type.IS);      callMatch("IS");
        MatchToken(Token_type.L_BRACKET);   callMatch("L_BRACKET");
        leftNode = Expression();
        Scale_x = semantic.GetExprValue(leftNode);
        MatchToken(Token_type.COMMA);   callMatch("COMMA");
        rightNode = Expression();
        Scale_y = semantic.GetExprValue(rightNode);
        MatchToken(Token_type.R_BRACKET);   callMatch("R_BRACKET");
        System.out.println("Exit from ScaleStatement.");
    }
    //RotStatement->ROT IS Expression
    public void RotStatement() throws IOException {
        ExprNode exprNode;
        System.out.println("Enter in RotStatement:");
        MatchToken(Token_type.ROT);     callMatch("ROT");
        MatchToken(Token_type.IS);      callMatch("IS");
        exprNode = Expression();
        Rot_ang = semantic.GetExprValue(exprNode);
        System.out.println("Exit from RotStatement.");
    }
    //ForStatement->FOR T FROM Expression TO Expression STEP Expression DRAW L_BRACKET Expression COMMA Expression R_BRACKET
    public void ForStatement() throws IOException {
        ExprNode startNode,endNode,stepNode,xNode,yNode;
        double start,end,step;
        System.out.println("Enter in ForStatement:");
        MatchToken(Token_type.FOR); callMatch("FOR");
        MatchToken(Token_type.T);   callMatch("T");
        MatchToken(Token_type.FROM);    callMatch("FROM");
        startNode = Expression();
        start = semantic.GetExprValue(startNode);
        MatchToken(Token_type.TO);      callMatch("TO");
        endNode = Expression();
        end = semantic.GetExprValue(endNode);
        MatchToken(Token_type.STEP);    callMatch("STEP");
        stepNode = Expression();
        step = semantic.GetExprValue(stepNode);
        MatchToken(Token_type.DRAW);    callMatch("DRAW");
        MatchToken(Token_type.L_BRACKET);   callMatch("L_BRACKET");
        xNode = Expression();
        MatchToken(Token_type.COMMA);   callMatch("COMMA");
        yNode = Expression();
        MatchToken(Token_type.R_BRACKET);   callMatch("R_BRACKET");
        semantic.DrawLoop(start,end,step,xNode,yNode);
        System.out.println("Exit from ForStatement");
    }
    //Expression->Term{(PLUS | MINUS) TERM}
    public ExprNode Expression() throws IOException {
        ExprNode leftNode,rightNode;
        Token_type token_tmp;
        System.out.println("Enter in Expression:");
        leftNode = Term();                          //分析左操作数得到左子树
        while (token.type==Token_type.PLUS || token.type==Token_type.MINUS){
            token_tmp = token.type;
            MatchToken(token_tmp);
            rightNode = Term();                     //得到右子树
            leftNode = MakeExprNode(token_tmp,leftNode,rightNode);  //构造操作数语法树
        }
        PrintSyntaxTree(leftNode,1);
        System.out.println("Exit from Expression.");
        return leftNode;
    }
    //Term->Factor{(MUL | DIV) Factor}
    public ExprNode Term() throws IOException {
        ExprNode leftNode,rightNode;
        Token_type token_tmp;
        //System.out.println("Enter in Term:");
        leftNode = Factor();
        while (token.type==Token_type.MUL || token.type==Token_type.DIV){
            token_tmp = token.type;
            MatchToken(token_tmp);
            rightNode = Factor();
            leftNode = MakeExprNode(token_tmp,leftNode,rightNode);
        }
        //System.out.println("Exit from Term.");
        return leftNode;
    }
    //Factor->PLUS Factor | MINUS Factor | Component
    public ExprNode Factor() throws IOException {
        ExprNode leftNode,rightNode;
        //System.out.println("Enter in Factor:");
        if (token.type==Token_type.PLUS){
            MatchToken(Token_type.PLUS);
            rightNode = Factor();
        }
        else if (token.type==Token_type.MINUS){
            MatchToken(Token_type.MINUS);
            rightNode = Factor();
            leftNode = new ConstNode();
            leftNode.Opcode = Token_type.CONST_ID;
            ((ConstNode)leftNode).Const = 0.0;
            rightNode = MakeExprNode(Token_type.MINUS,leftNode,rightNode);
        }
        else rightNode = Component();
        //System.out.println("Exit from Factor.");
        return rightNode;
    }
    //Component->Atom POWER Component | Atom
    public ExprNode Component() throws IOException {
        ExprNode leftNode,rightNode;
        leftNode = Atom();
        if (token.type==Token_type.POWER){
            MatchToken(Token_type.POWER);
            rightNode = Component();
            leftNode = MakeExprNode(Token_type.POWER,leftNode,rightNode);
        }
        return leftNode;
    }
    //Atom->CONST_ID | T | FUNC L_BRACKET Expression R_BRACKET | L_BRACKET Expression R_BRACKET
    public ExprNode Atom() throws IOException {
        ExprNode node = null,tmp;
        Token t = token;
        switch (token.type){
            case CONST_ID:
                MatchToken(Token_type.CONST_ID);
                node = MakeExprNode(Token_type.CONST_ID,t.value);
                break;
            case T:
                MatchToken(Token_type.T);
                node = MakeExprNode(Token_type.T);
                break;
            case FUNC:
                MatchToken(Token_type.FUNC);
                MatchToken(Token_type.L_BRACKET);
                tmp = Expression();
                node = MakeExprNode(Token_type.FUNC,t.funcName,tmp);
                MatchToken(Token_type.R_BRACKET);
                break;
            case L_BRACKET:
                MatchToken(Token_type.L_BRACKET);
                node = Expression();
                MatchToken(Token_type.R_BRACKET);
                break;
            default:    SyntaxError(2);
        }
        return node;
    }
    public static void main(String[] args) throws IOException {
        Parser parser = new Parser();
        //Parser.paramater.value = 6.0;
        parser.FetchToken();
        parser.Program();
        lexical.CloseScanner(pis);


    }

}
