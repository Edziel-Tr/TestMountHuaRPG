/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.gamev4e;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BattleDialogue extends javax.swing.JFrame {

    private Player player;
    private Enemy enemy;
    private JTextArea battleLog;
    private JPanel actionPanel;
    private JPanel statsPanel;


    public BattleDialogue(JFrame parent, Player player, Enemy enemy) {
        super("Battle"); // Correct JFrame constructor
        this.player = player;
        this.enemy = enemy;
        
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        initComponents();
    }
     private void initComponents() {
        // Battle log area
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setFont(new Font("KaiTi", Font.PLAIN, 14));
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBackground(new Color(20, 20, 40));
        battleLog.setForeground(Color.WHITE);
        
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Battle Log"));
        
        // Stats panel
        statsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        updateStats();
        
        // Action buttons
        actionPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        String[] actions = {"Attack", "Use Skill", "Defend", "Flee"};
        for (String action : actions) {
            JButton button = new JButton(action);
            button.setFont(new Font("KaiTi", Font.BOLD, 14));
            button.addActionListener(this::handleBattleAction);
            actionPanel.add(button);
        }
        
        // Layout
        add(logScroll, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.NORTH);
        add(actionPanel, BorderLayout.SOUTH);
        
        // Initial battle message
        appendLog("\n--- BATTLE ---");
        appendLog("Enemy: " + enemy.name + " | Affiliation: " + enemy.sect + " | Realm: " + GameV4e.REALMS[enemy.realmIndex]);
    }
    
    private void updateStats() {
        statsPanel.removeAll();
        
        // Player stats
        JPanel playerPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        playerPanel.setBorder(BorderFactory.createTitledBorder(player.name));
        
        JLabel playerHp = new JLabel("HP: " + player.hp + "/" + player.getMaxHp());
        JLabel playerQi = new JLabel("Qi: " + player.qi + "/" + player.getMaxQi());
        JLabel playerAtkDef = new JLabel("ATK: " + player.getAtk() + " DEF: " + player.getDef());
        
        playerPanel.add(playerHp);
        playerPanel.add(playerQi);
        playerPanel.add(playerAtkDef);
        
        // Enemy stats
        JPanel enemyPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        enemyPanel.setBorder(BorderFactory.createTitledBorder(enemy.name));
        
        JLabel enemyHp = new JLabel("HP: " + enemy.hp);
        JLabel enemyAtkDef = new JLabel("ATK: " + enemy.atk + " DEF: " + enemy.def);
        JLabel enemyRealm = new JLabel("Realm: " + GameV4e.REALMS[enemy.realmIndex]);
        
        enemyPanel.add(enemyHp);
        enemyPanel.add(enemyAtkDef);
        enemyPanel.add(enemyRealm);
        
        statsPanel.add(playerPanel);
        statsPanel.add(enemyPanel);
        
        statsPanel.revalidate();
        statsPanel.repaint();
    }
    
    private void handleBattleAction(ActionEvent e) {
        String action = ((JButton)e.getSource()).getText();
        
        switch (action) {
            case "Attack":
                performBasicAttack();
                break;
            case "Use Skill":
                useSkill();
                break;
            case "Defend":
                defend();
                break;
            case "Flee":
                attemptFlee();
                break;
        }
        
        if (enemy.hp > 0) {
            enemyTurn();
        } else {
            battleWon();
            return;
        }
        
        if (player.hp <= 0) {
            battleLost();
            return;
        }
        
        updateStats();
    }
    
    private void performBasicAttack() {
        if (Math.random() < 0.1) {
            appendLog("You missed!");
        } else {
            int dmg = Math.max(1, player.getAtk() - enemy.def);
            enemy.hp -= dmg;
            appendLog("You attack for " + dmg + " damage!");
        }
    }
    
    private void useSkill() {
        if (player.skills.isEmpty()) {
            appendLog("No skills available!");
            return;
        }
        
        String skill = (String) JOptionPane.showInputDialog(
            this,
            "Select skill to use:",
            "Use Skill",
            JOptionPane.PLAIN_MESSAGE,
            null,
            player.skills.toArray(),
            player.skills.get(0));
        
        if (skill == null) return;
        
        int mastery = player.skillMastery.getOrDefault(skill, 1);
        int skillPower = 10 + mastery * 5;
        
        if (player.skillCooldown.get(skill) > 0) {
            appendLog(skill + " is on cooldown!");
            return;
        }
        
        if (player.qi < 10) {
            appendLog("Not enough Qi!");
            return;
        }
        
        int dmg = Math.max(1, player.getAtk() + skillPower - enemy.def);
        enemy.hp -= dmg;
        appendLog("You use " + skill + " for " + dmg + " damage!");
        player.gainSkillExp(skill, 5);
        player.qi -= 10;
        player.skillCooldown.put(skill, 2);
    }
    
    private void defend() {
        appendLog("You take a defensive stance! Next attack will be halved.");
        // Defense logic would be handled in enemyTurn()
    }
    
    private void attemptFlee() {
        if (Math.random() < 0.5) {
            appendLog("You fled successfully!");
            dispose();
        } else {
            appendLog("Failed to flee!");
        }
    }
    
    private void enemyTurn() {
        if (Math.random() < 0.1) {
            appendLog(enemy.name + " missed!");
        } else {
            int dmg = Math.max(1, enemy.atk - player.getDef());
            // TODO: Handle defense halving
            player.hp -= dmg;
            appendLog(enemy.name + " attacks for " + dmg + " damage!");
        }
    }
    
    private void battleWon() {
        appendLog("Victory! You defeated " + enemy.name);
        player.gainExp(15 + 7 * enemy.realmIndex);
        GameV4e.achievements.add("First Victory");
        Battle.checkQuestProgress(player, "defeat", enemy.sect);
        
        JOptionPane.showMessageDialog(this, 
            "You have defeated " + enemy.name + "!\nGained " + (15 + 7 * enemy.realmIndex) + " EXP.", 
            "Victory", 
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    private void battleLost() {
        appendLog("You have been defeated...");
        player.hp = player.getMaxHp() / 2;
        
        JOptionPane.showMessageDialog(this, 
            "You were defeated by " + enemy.name + ".\nYou wake up with half your HP.", 
            "Defeat", 
            JOptionPane.WARNING_MESSAGE);
        
        dispose();
    }
    
    private void appendLog(String text) {
        battleLog.append(text + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BattleDialogue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BattleDialogue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BattleDialogue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BattleDialogue.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        try {
        for (javax.swing.UIManager.LookAndFeelInfo info : 
             javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (ClassNotFoundException ex) {
        java.util.logging.Logger.getLogger(BattleDialogue.class.getName())
            .log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
        java.util.logging.Logger.getLogger(BattleDialogue.class.getName())
            .log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
        java.util.logging.Logger.getLogger(BattleDialogue.class.getName())
            .log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
        java.util.logging.Logger.getLogger(BattleDialogue.class.getName())
            .log(java.util.logging.Level.SEVERE, null, ex);
    }

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            try {
                Player player = new Player();
                Npc_Player npc = new Npc_Player();
                Enemy enemy = new Enemy(npc);
                new BattleDialogue(null, player, enemy).setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error initializing battle: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    });
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
