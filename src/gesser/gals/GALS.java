package gesser.gals;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

/*
 * TODO: Problema de contexto sem nada depois
 * TODO: Simulador Passo a Passo
 * TODO: Comprimir Tabelas Léxico - C++ 
 * TODO: Comprimir Tabelas Léxico - Delphi
 * TODO: Comprimir Tabelas Sintático - Java
 * TODO: Comprimir Tabelas Sintático - C++
 * TODO: Comprimir Tabelas Sintático - Delphi
 * TODO: Mostrar AF em modo gráfico
 * TODO: Melhorar msgs de erro para (S|LA)?LR
 * TODO: Eliminar Calculo repedido do AF (qd sintatico pega lista de tokens)
 * TODO: Reabilitar transformações
 */

public class GALS
{
    public static void centralize(Component c)
    {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Point center = new Point(d.width/2, d.height/2);
        c.setLocation(center.x-c.getWidth()/2, center.y-c.getHeight()/2);
    }
    
    public static void main(String[] args)
    {
        MainWindow window = MainWindow.getInstance();
        centralize(window);
   
        window.setVisible(true);
    }
}
