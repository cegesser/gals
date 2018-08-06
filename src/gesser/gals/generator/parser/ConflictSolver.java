package gesser.gals.generator.parser;

import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import gesser.gals.MainWindow;
import gesser.gals.generator.parser.ll.LLConflictSolver;
import gesser.gals.generator.parser.lr.Command;
import gesser.gals.generator.parser.lr.LRConflictSolver;
import gesser.gals.util.IntegerSet;

import static gesser.gals.generator.parser.lr.Command.Type.*;

public class ConflictSolver extends Box implements LLConflictSolver, LRConflictSolver
{
    private static final ConflictSolver instance = new ConflictSolver();
    public static ConflictSolver getInstance(){ return instance; }
   
       
    private DefaultListModel conflictListModel = new DefaultListModel();
    private JList conflictList = new JList(conflictListModel);
    private JLabel label1 = new JLabel();
    private JLabel label2 = new JLabel();
    private JLabel label3 = new JLabel();
    
    private ConflictSolver()
    {
        super(BoxLayout.Y_AXIS);
        
        add(new JLabel("Ocorre um conflito quando:"));                
        add(label1);
        add(label2);
        add(Box.createVerticalStrut(10));
        add(label3);
        add(Box.createVerticalStrut(10));
        add(Box.createVerticalGlue());
        add(new JScrollPane(conflictList));
    }
    
    /**
     * LL Conflict Resolution
     */

    public int resolve(Grammar g, IntegerSet conflict, int input, int stackTop)
    {        
        String in;
        if (input == 0)
            in = "$";
        else
            in = g.getTerminals()[input-1];

        label1.setText("- O símbolo no topo da pilha é: "+g.getNonTerminals()[stackTop]);
        label2.setText("- O símbolo da entrada é: "+in);
        label3.setText("Qual produção deve ser utilizada?");
                
        List<Production> pl = g.getProductions();
                
        conflictListModel.removeAllElements();
        for (Integer i : conflict)
        {
            conflictListModel.addElement(new ProductionItem(pl.get(i.intValue()).toString(), i.intValue()));
        }
        
        return showDialog();  
    }
    
    /**
     *  LR Conflict Resolution
     */

    public int resolve(Grammar g, Command[] conflict, int state, int input)
    {
        String in;
        if (input == 0)
            in = "$";
        else
            in = g.getTerminals()[input-1];

        label1.setText("- O estado no topo da pilha é: "+state);
        label2.setText("- O símbolo da entrada é: "+in);
        label3.setText("Qual ação a ser executada:");
        
        conflictListModel.removeAllElements();
        for (int i=0; i<conflict.length; i++)
        {
            String label;
            switch (conflict[i].getType())
            {
                case REDUCE:
                    label = "Reduzir, pela produção "+conflict[i].getParameter();
                    break;
                case ACTION:
                    label = "Executar ação semântica "+conflict[i].getParameter();
                    break;
                case SHIFT:
                    label = "Empilhar \""+in+"\"";
                    break;
                default:
                    label = conflict[i].toString();
                    break;
            }
            conflictListModel.addElement(new ProductionItem(label, i));
        }

        return showDialog();
    }
    
    private int showDialog()
    {
        conflictList.setSelectedIndex(0);
    
        JOptionPane.showMessageDialog(MainWindow.getInstance(), this);
    
        return  ((ProductionItem)conflictList.getSelectedValue()).getIndex();
    }
    
    private static class ProductionItem
    {
        private int index;
        private String label;
        
        
        public ProductionItem(String label, int index)
        {
            this.index = index;
            this.label = label;
        }
                
        public String toString()
        {
            return label;
        }
        
        public int getIndex()
        {
            return index;
        }
    }
}
