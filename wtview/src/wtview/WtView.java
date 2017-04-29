package wtview;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * �ŤߤĤ������ӥ塼��
 * <p>
 * �������ڤ��ؤ��ؼ����������˼�����ɽ�����롣ɽ����Υ桼�����쥹�ݥ�(ɽ�����֡���󥭥󥰥��å�/������ꥯ������)
 * �˽��äơ�����ɽ��������Ψ����ư���롣<p>
 * ver2.0�Ǥ��ѹ���
 * <ul>
 * <li>��ư���˿�����Ͽ�����򥹥���󤷤�ɽ������褦�ˤ��뢪��Ϥ�Register�ǽ������٤�
 * <li>�ե������ư���б�������
 * <li>�ޥ�����å����ؤ��б�
 * <li>1��Υ��å����Ǥ�ɽ����1������ˤ��롣�������ꡢ�������ȥ��󥰤��������
 * <li>ɽ���Υ�󥭥��ϰϻ���
 * <li>ɾ�����ȥ�ƥ����β���
 *  <ul>
 *  <li>ɽ�����֤�ɾ���������Ϥ���礷�ƥ�󥭥󥰤�����
 *  <li> ɽ������ɾ���λ�ɸ�Ȥ��ơ����ߤ�ɽ�����֤ΰ�ưʿ�ѤȤ���Ӥ��Ѥ���
 *  <li>�������ϤȤ��ơ���󥯥��å�/������/���ޤ������ߤ���
 *  <li>ɽ������ɾ���γ��/���ߥåȤ�����դ��롣�׻���ߥ������դ���?
 *  </ul>
 * </ul>
 */
class WtView {
	static ShowStrategy ss;
	private static JFrame f;
	private static SimpleView sv;

	/**
	 * �Ťߥǡ����١������ɤ߹��ߡ����å����򥹥����Ȥ����롣���å������ڤ��ؤ���Alt+�����ǹԤ�
	 * @param args �Ťߥǡ����١����Υե�����̾���¤�
	 */
	public static void main(String[] args) {
		f = new JFrame();
		sv = new SimpleView();
		f.getContentPane().add(sv);
		ss = new ShowStrategy(args, sv, f);

		f.setSize(1280, 800);
		f.setVisible(true);
		sv.requestFocusInWindow();
	}
	
	static void next() {
		ss.next();
	}
	/**
	 * ���å������ڤ��ؤ���
	 * @param sessionNo�����å�����ֹ档�ͤ���ʤ饨�����ȥ�⡼�ɤΥȥ���
	 */
	static void changeSession(int sessionNo) {
		ss.changeSession(sessionNo);
	}
	static void quit() {
		int n = ss.terminate();
		f.setVisible(false);
		JOptionPane.showMessageDialog(null, n + " files displayed");
		System.exit(0);
	}
}
