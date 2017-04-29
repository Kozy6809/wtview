package wtview;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.awt.*;
/**
   ɽ�������������ξ�����ݻ����륪�֥�������
*/
class ViewElement implements Serializable {
	/**
	 * <code>serialVersionUID</code> �Υ�����
	 */
	private static final long serialVersionUID = 1L;
	
    int wt = 0; // �Ť�
    String name; // �ե�ѥ�̾
    double mag = 1.0; // ɽ����Ψ
    int refs = 0; // ���Ȳ��
    long ts =0; // �Ǹ��ɽ�����줿���Υ����ॹ�����
    transient volatile Image img;
    transient volatile Image simg; // ��������󥰤��줿����
    transient long laps = 0; // �ѻ�ɽ������
    transient long time0; // �׻��ε���
    transient boolean timeOn = false; // �׻���
    transient int up = 0; // ��󥯥��åץꥯ�����Ȳ��
    transient int down = 0; // ��󥯥�����ꥯ�����Ȳ��
    transient boolean extra = false; // �������ȥ��󥰻���

    void startDspTime() {
    	if (timeOn) return;
    	time0 = System.currentTimeMillis();
    	timeOn = true;
    }
    void stopDspTime() {
    	if (!timeOn) return;
    	laps += (System.currentTimeMillis() - time0);
    	timeOn = false;
    }
    
    /**
     * �̤�ViewElement���ڤ��ؤ���ݤ�ɽ����λ��³��
     *
     */
    void changeVe() {
    	stopDspTime();
    	ts = System.currentTimeMillis();
    }
    
    void flush() {
    	// �꥽��������
    	if (img != null) img.flush();
    	if (simg != null) simg.flush();
    	img = null;
    	simg = null;
    	if (Runtime.getRuntime().freeMemory() < 10000000L) System.gc();
    }
}
