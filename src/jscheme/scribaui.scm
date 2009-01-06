(import "javax.swing.*")
(import "seco.*")
(import "seco.gui.*")
(import "seco.rtenv.*")
(import "seco.notebook.gui.*")

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
 
(define (install-runtime-menu)
  (SwingUtilities.invokeLater (lambda ()
   (let ((rMenu (runtime-menu)) 
   		 (idx (find-menu "Runtime"))
   		 (bar (.getBar desktop))
   	    )
       (.setMnemonic rMenu #\r)
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

(define (center-component C)
(GUIUtilities.centerOnScreen C))

 
