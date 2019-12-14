package com.hit.demo;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import javax.swing.*;
import java.awt.Graphics;
import java.util.Random;


public class Semantic {
    JLabel view;
    BufferedImage surface;
    JFrame frame;
    Graphics g;


    public Semantic(){
        surface = new BufferedImage(1000,1000,BufferedImage.TYPE_INT_RGB);
        //surface.setRGB(0,0,0);
        view = new JLabel(new ImageIcon(surface));
        view.setBackground(Color.WHITE);
        g = surface.getGraphics();
        g.fillRect(0,0,1000,1000);
        frame = new JFrame();
        int vertex = 300;
        int convasSize = vertex*vertex;
        //frame.setSize(convasSize,convasSize);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this.view);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }


//    public void  paint(Graphics  g,int hor_x,int ver_y)
//    {
//        super.paint(g);//加上这一句，窗体背景色就会画出来
//        Graphics gr = g;
//        gr.setColor(Color.RED);
//        gr.fillOval(hor_x, ver_y, 2, 2);
//    }

    //判断函数类型并计算函数值
    public double calFunc(FuncName funcName, Double value){
        double result=0;
        switch (funcName){
            case sin: result = Math.sin(value);break;
            case cos: result = Math.cos(value);break;
            case tan: result = Math.tan(value);break;
            case log: result = Math.log(value);break;
            case exp: result = Math.exp(value);break;
            case sqrt: result = Math.sqrt(value);break;
        }
        return result;
    }
    //计算并返回节点表达式的值
    public double GetExprValue(ExprNode root){
        if (root==null){
            return 0.0;
        }
        switch (root.Opcode){
            case PLUS:
                return GetExprValue(((OperatorNode)root).Left)+GetExprValue(((OperatorNode)root).Right);
            case MINUS:
                return GetExprValue(((OperatorNode)root).Left)-GetExprValue(((OperatorNode)root).Right);
            case MUL:
                return GetExprValue(((OperatorNode)root).Left)*GetExprValue(((OperatorNode)root).Right);
            case DIV:
                return GetExprValue(((OperatorNode)root).Left)/GetExprValue(((OperatorNode)root).Right);
            case POWER:
                return Math.pow(GetExprValue(((OperatorNode)root).Left),GetExprValue(((OperatorNode)root).Right));
            case CONST_ID:
                return ((ConstNode)root).Const;
            case T:
                return ((ParmNode)root).Parm.value;
            case FUNC:
                return calFunc(((FuncNode)root).Func,GetExprValue(((FuncNode)root).Child));
            default:
                return 0.0;
        }
    }
    //计算点的坐标值
    public Double[] CalcCoord(ExprNode Hor_Exp, ExprNode Ver_Exp){
        double HorCord,VerCord,Hor_tmp;
        Double cors[] = new Double[2];
        //计算得到点的原始坐标
        HorCord = GetExprValue(Hor_Exp);
        VerCord = GetExprValue(Ver_Exp);
        //进行比例变换
        HorCord *= Parser.Scale_x;
        VerCord *= Parser.Scale_y;
        //进行旋转变换
        Hor_tmp = HorCord * Math.cos(Parser.Rot_ang)+VerCord*Math.sin(Parser.Rot_ang);
        VerCord = VerCord * Math.cos(Parser.Rot_ang) -HorCord*Math.sin(Parser.Rot_ang);
        HorCord = Hor_tmp;
        //进行平移变换
        HorCord += Parser.Origin_x;
        VerCord += Parser.Origin_y;
        //传给x,y相当于返回变换后的点坐标
        cors[0]=HorCord;
        cors[1]=VerCord;
        return cors;
    }
    //绘制一个像素点
    public void DrawPixel(Integer hor_x, Integer ver_y,Graphics g){
        g.setColor(Color.BLACK);
        g.fillOval(hor_x,ver_y,3,3);
    }

    //循环绘制点坐标
    public void DrawLoop(double start, double end, double step, ExprNode HorNode, ExprNode VerNode){
        Double cors[] = new Double[2];
        Double x;
        Double y;
        for (Parser.paramater.value=start;Parser.paramater.value<=end;Parser.paramater.value+=step){
            cors = CalcCoord(HorNode,VerNode);
            x = cors[0];
            y = cors[1];
            //System.out.println("x="+x+" y="+y);
            DrawPixel(x.intValue(),y.intValue(),g);
        }
    }

    public static void main(String[] args){
        Semantic semantic = new Semantic();
        Parser parser = new Parser();
        ConstNode leftConst = new ConstNode();
        leftConst.Const = 2;
        leftConst.Opcode = Token_type.CONST_ID;
        ConstNode rightConst = new ConstNode();
        rightConst.Const = 3;
        rightConst.Opcode = Token_type.CONST_ID;
        ExprNode leftNode = parser.MakeExprNode(Token_type.CONST_ID,2);
        ExprNode rightNode = parser.MakeExprNode(Token_type.CONST_ID,3);
        ExprNode root = parser.MakeExprNode(Token_type.PLUS,leftNode,rightNode);
        double res = semantic.GetExprValue(root);
        semantic.DrawLoop(100,200,1.0,leftNode,rightNode);
//        for (int i=0;i<1000;i++){
//            semantic.DrawPixel(i,2*i,semantic.g);
//        }
        //System.out.println(res);

        //semantic.DrawPixel(100,100,semantic.g);
        //semantic.DrawPixel(200,200,semantic.g);
    }
}
