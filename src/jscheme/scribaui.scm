(import "javax.swing.*")
(import "seco.*")
(import "seco.gui.*")
(import "seco.rtenv.*")
(import "seco.notebook.gui.*")
(import "seco.talk.*")

(use-module "elf/basic.scm")
(use-module "jlib/Swing.scm")
(use-module "jscheme/hglib.scm")

(define (find-menu text)
 (let loop ((i 0) (bar (.getBar  desktop))) 
    (if (= i  (.getMenuCount bar)) 
        -1
        (if (.equals (.getText (.getMenu bar i)) text)
            i
            (loop (+ i 1) bar)))))
            
(define (has-menu? text) (!= -1 (find-menu text)))

(define (update-menu-bar)
  (install-menu runtime-menu "Runtime" #\r)
  (install-menu network-menu "Network" #\n)
)

(define (install-menu the-menu title mnemonic)
  (SwingUtilities.invokeLater (lambda ()
   (let ((rMenu (the-menu)) 
   		 (idx (find-menu title))
   		 (bar (.getBar desktop))
   	    )
       (.setMnemonic rMenu mnemonic)
       (if (> idx -1) 
           (begin (.remove bar idx) (.add bar rMenu idx))
	       (.add bar rMenu))
	   (.updateUI bar)
  )))
)

(define (runtime-menu)
  (menu "Runtime"
      (menuitem "New Context" (action (lambda (e) 
          (.show (edit-context-dialog #null (RuntimeContext.)))
      )))
      (menuitem "Configure Current" (action (lambda (e) 
          (let ((h (.getCurrentRuntimeContext desktop)))
              (.show (edit-context-dialog h (.get niche h))))
      )))
      (menuitem "Manage Contexts" (action (lambda (e) 
          (.show (manage-contexts-dialog)))))
  )
)

(define (network-menu)
  (menu "Network"
    (menuitem "Peer List" (action (lambda (e) (.println System.out$ "Open Peer list"))))
  )
)

(define (manage-contexts-dialog)
  (letrec (
     (hSel #null)
     (find-context (lambda (name)
       (hg:with-rs rs (type RuntimeContext.class)
          (let loop () (if (.hasNext rs) (let ((h (.next rs))) (if (.equals (.getName (.get niche h)) name) h (loop))) #null)))))
     (get-selection (lambda () 
           (let* ( (idx (.getSelectedIndex (.getView (.getViewport listBox)))))
              (if (< idx 0)
                   #null		
                  (let ((h (find-context (.getElementAt model idx))))
	  (if (eq? h #null) null (cons h (.get niche h))))))))
     (model (let ((m (DefaultListModel.))) (map (lambda (h) (.addElement m (.getName (.get niche h)))) 
                                   (hg:with-rs rs (type RuntimeContext.class) (hg:rs->list rs))) m)) 
     (listBox (scrollpane (JList. model)))
    (D (dialog desktop #t "Edit Runtime Context" 
    (row 'north 'both listBox
        'north 'horizontal (col
           (button "Edit" (action (lambda (e) 
                  (let ((ctx (get-selection))) (if (not (eq? ctx #null)) (.show (edit-context-dialog (car ctx) (cdr ctx))))))))
           (button "Reboot" (action (lambda (e) 
                  (let ((ctx (get-selection))) (if (not (eq? ctx #null)) (.reboot (ThisNiche.getEvaluationContext (car ctx))))))))
           (button "Delete" (action (lambda (e) 
                 (let ((ctx (get-selection))) (if (not (eq? ctx #null)) (begin (.remove niche (car ctx)) (.removeElement (.getModel listBox) (.getName (cdr ctx)))))))))
           (button "Close" (action (lambda (e) (.setVisible D #f))))
        )
      )))
    )
    (.setSize D 200 200)
    (center-component D)
    D
))

(define (show-error msg) 
   (JOptionPane.showMessageDialog #null msg "Error" JOptionPane.ERROR_MESSAGE$))

(define (save-scriba-context handle ctx)
  (let ((bindings (.getBindings ctx)))
      (.setBindingsEx ctx (javax.script.SimpleBindings.))
      (if (eq? handle #null) (set! handle (.add niche ctx)) (.replace niche handle ctx))
      (.setBindingsEx ctx bindings)
      handle
   )
)

(define (edit-context-dialog handle runtimeContext)
  (letrec   (    
    (items (CPListModel. (.getClassPath runtimeContext)))
    (save (lambda ()  (let ((name (.getText nameField)))
              (if (= (.length name) 0)  
                  (begin (show-error "Context name cannot be empty.") #f)
                  (begin (.setClassPath runtimeContext (.getClassPath items))
                         (.setName runtimeContext name)
                         (set! handle (save-scriba-context handle runtimeContext))
                         #t
                ))))
    )
    (chooser (JFileChooser.))
    (the-list (JList. items))
    (listBox (scrollpane the-list))
    (nameField (textfield (.getName runtimeContext)))
    (D (dialog desktop #t "Edit Runtime Context" 
     (border 
         (north (col (border (west (flow (label "Context Name") nameField)))   (label "Class Path") ))
         (center            
                  (row 'north  listBox 'both
  	(border (north (col
  		  (button "Add ClassPath Entry"
	           (action (lambda (e) 
	              (if (= (.showDialog chooser desktop "Select JAR or Directory") JFileChooser.APPROVE_OPTION$)
                      (iterate (.getSelectedFiles chooser) (lambda (f) (.addEntry items (ClassPathEntry. f))))
                                   ))))
	        (button "Remove Selected"
	                (action (lambda (e) (tryCatch 
                                       (.removeEntries items (.getSelectedIndices the-list))
                                       (lambda (e) (.printStackTrace e))))))))
           )))
          (south (border (east (flow
	(button "Ok" (action (lambda (e) (if (save) (.setVisible D #f)))))
	(button "Apply" (action (lambda (e) (save))))
                    (button "Cancel" (action (lambda (e) (.setVisible D #f))))
          ))))
       )
     ))
    )
   (.setMultiSelectionEnabled chooser #t)
   (.setFileSelectionMode chooser javax.swing.JFileChooser.FILES_AND_DIRECTORIES$)
   (.setSize D (size 700 500))
   (center-component D)
  D
 )
)

(define (user-msg msg) 
   (JOptionPane.showMessageDialog #null msg "Message From the System" JOptionPane.INFORMATION_MESSAGE$))
(define checkbox (lambda R
  (let ((T (javax.swing.JCheckBox.)))
  (processConArgs T R))))

(define (open-network-connection settings)
    (define host (textfield (.getHostname settings) 25))
    (define port (textfield (.toString (.getPort settings)) 6))
    (define user-label (label "Username:"))
    (define pwd-label (label "Password:"))
    (define username (textfield (.getUsername settings) 15))
    (define password (JPasswordField. (.getPassword settings) 15))
    (define check-register (checkbox))
    ; Proxy settings fields
    (define proxy-user-label (label "Proxy Username:"))
    (define proxy-pwd-label (label "Proxy Password:"))
    (define proxy-host-label (label "Proxy Host:"))
    (define proxy-port-label (label "Proxy Port:"))
    (define proxy-username (textfield (.getProxyUser settings) 15))
    (define proxy-password (JPasswordField. (.getProxyPass settings) 15))
    (define proxy-host (textfield (.getProxyHost settings) 25))
    (define proxy-port (textfield (.toString (.getProxyPort settings)) 6))
    (define check-proxy (checkbox (action (lambda (e)
      (if (.isSelected check-proxy)
          (begin (for-each (lambda (x) (.setEnabled x #t)) (list proxy-host proxy-port proxy-username proxy-password))
                     (for-each (lambda (x) (.setForeground x black)) (list proxy-user-label proxy-pwd-label proxy-host-label proxy-port-label)))
          (begin (for-each (lambda (x) (.setEnabled x #f)) (list proxy-host proxy-port proxy-username proxy-password))
                     (for-each (lambda (x) (.setForeground x gray)) (list proxy-user-label proxy-pwd-label proxy-host-label proxy-port-label)))
      )
    ))))
    (define check-anonymous (checkbox (action (lambda (e) 
      (if (.isSelected check-anonymous)
          (begin (.setEnabled username #f) (.setEnabled password #f) (.setForeground user-label gray) (.setForeground pwd-label gray))
          (begin (.setEnabled username #t) (.setEnabled password #t) (.setForeground user-label black) (.setForeground pwd-label black))
      )   
    ))))
    (define connect-button (button "Connect" (action (lambda (e)
      (seco.talk.ConnectionManager.openConnectionPanel settings)
      (.setVisible networkDialog #f)
    ))))
    (define test-button (button "Test" (action (lambda (e)
      (user-msg "Not implemented yet.")
    ))))
    (define save-button (button "Save" (action (lambda (e)
      (.setHostname settings (.getText host))
      (.setPort settings (Integer.parseInt (.getText port)))
      (.setAnonymousLogin settings (.isSelected check-anonymous))
      (.setUsername settings (.getText username))
      (.setPassword settings (.getText password))
      (.setAutoRegister settings (.isSelected check-register))
      (.setUseProxy settings (.isSelected check-proxy))
      (.setProxyHost settings (.getText proxy-host))
      (.setProxyPort settings (Integer.parseInt (.getText proxy-port)))
      (.setProxyUser settings (.getText proxy-username))
      (.setProxyPass settings (.getText proxy-password))
      (.update niche settings)
    ))))
    (define close-button (button "Close" (action (lambda (e)
      (.setVisible networkDialog #f)
    ))))
    (define networkDialog (dialog desktop #f "Network Connection"
      (border (north
        (col 'horizontal 
              (border (west (flow (label "Host") host)))
              (border (west (flow (label "Port ") port)))
              (border (west (flow (label "Login Anonymously") check-anonymous)))
              (border (west (flow user-label username)))
              (border (west (flow pwd-label password)))
              (border (west (flow (label "Register Automatically ") check-register)))
              (border (west (flow (label "Use Proxy") check-proxy)))
              (border (west (flow proxy-host-label proxy-host)))
              (border (west (flow proxy-port-label proxy-port)))
              (border (west (flow proxy-user-label proxy-username)))
              (border (west (flow proxy-pwd-label proxy-password)))
              (border (west (flow connect-button save-button  close-button)))
        )
    ))))
    (.setBounds networkDialog 100 100 500 500)
    (.setVisible networkDialog #t)
    (.setSelected check-anonymous (.isAnonymousLogin settings))
    (.setSelected check-register (.isAutoRegister settings))
    (.setSelected check-proxy #t)
    (.setSelected check-proxy (.isUseProxy settings))
    (center-component networkDialog)
    (.pack networkDialog)
)
         
(define (netdialog-action e) 
  (let ((config (hg:with-rs rs (type ConnectionPanel.class) 
                  (if (.hasNext rs) (.getConnectionConfig (.get ThisNiche.hg$ (.next rs))) 
                      (seco.talk.ConnectionConfig.)))))
        (if (eq? #null (.getHandle ThisNiche.hg$ config))
            (.add ThisNiche.hg$ config))
        (open-network-connection config)
))
      
(define (center-component C)
	(GUIUtilities.centerOnScreen C))

 
