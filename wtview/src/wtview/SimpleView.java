package wtview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * 指定された画像を表示する。キー・コマンドを受け取って各種操作を行う。<p>
 * このクラス内部で処理されるコマンド<p>
 * 画像の拡大縮小<p>
 * 画像のランキングアップ/ダウン
 * <p>
 * その他のコマンドはWtViewに投げる
 * <ul>
 * <li>次画像表示
 * <li>終了
 * <li>画像をエキストラ指定
 * <li>計時の中断/再開
 * <li>セッション切り替え
 * </ul>
 *
 * <ul>
 *
 */
class SimpleView extends JScrollPane implements Runnable {
	private volatile ViewElement ve; // 現在表示中のViewElement
	// 画像を準備するエレメントのリスト
	private BlockingQueue<ViewElement> picQueue = new LinkedBlockingQueue<ViewElement>();
	
	private enum Mag {
		MAGNIFY, SHRINK, FIT_WIDTH, FIT_HEIGHT, X1, X2
	};

	private ImgPanel viewPanel = new ImgPanel();

	private Image img;

	SimpleView() {
		super(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setViewportView(viewPanel);
		KeyAdapter ka = new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				handleKey(e);
			}
		};
		addKeyListener(ka);
		Thread t = new Thread(this);
		//t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		
	}

	/**
	 * スケーリング画像の生成を専用スレッドで実施する
	 */
	public void run() {
		for (;;) {
			ViewElement p = null;
			try {
				p = picQueue.take();
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			if (p.img == null) continue;
			int w = p.img.getWidth(this);
			int h = p.img.getHeight(this);
			int s_w = (int) Math.round(w * p.mag);
			int s_h = (int) Math.round(h * p.mag);
			BufferedImage s = new BufferedImage(s_w, s_h, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = s.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			AffineTransform at = AffineTransform.getScaleInstance(p.mag, p.mag);
			g.drawImage(p.img, at, null);
			p.simg = s;
			if (p == ve) drawImage(p.simg, p); // 生成された画像が今表示すべきものの場合
		}
	}
	/**
	 * 画面に表示されるサイズを返す
	 */
	private Dimension getExtentSize() {
		return getViewport().getExtentSize();
	}

	/**
	 * 指定された画像を表示する
	 * 
	 * @param ve
	 *            表示対象のViewElement
	 * @return 表示に成功すればtrue
	 */
	boolean showVe(ViewElement ve) {
		if (ve == null) return false;
		if (this.ve != null) this.ve.changeVe();
		this.ve = ve;
		if (!prepareImage(ve)) return false;
		
		// 表示すべきスケーリング画像がまだ生成されていない場合はリターンする。
		// 実際の表示は生成完了した際に行われる
		if (ve.mag != 1.0 && ve.simg == null) return true;
		
		drawImage((ve.mag == 1.0) ? ve.img : ve.simg, ve);

		return true;
	}
	
	/**
	 * 画像の準備をする
	 * @param ve　準備対象のViewElement
	 * @return 準備に成功すればtrue
	 */
	boolean prepareImage(ViewElement ve) {
		if (ve.img == null) {
			try {
				BufferedImage bi = ImageIO.read(new File(ve.name));
				ve.img = bi;
			} catch (IOException e) {
				System.out.println("can't read " + ve.name);
				return false;
			}
		}
		
		if (ve.mag != 1.0 && ve.simg == null) try {
			picQueue.put(ve);
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * 指定された画像を表示する。
	 * @param img 表示するイメージ
	 * @param timeVe 計時対象のviewElement。
	 */
	private void drawImage(Image img, final ViewElement timeVe) {
		this.img = img;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				timeVe.startDspTime();
				setViewportView(viewPanel);
			}
		});
	}

	/**
	 * 倍率コードに基づきスケーリングされた画像を表示する
	 * 
	 * @param mag 倍率コード
	 */
	private void showScaledImage(Mag mag) {
		Dimension d = getExtentSize();
		int w = ve.img.getWidth(this);
		int h = ve.img.getHeight(this);

		switch (mag) {
		case X1: // 等倍
			ve.mag = 1.0;
			drawImage(ve.img, ve);
			return;
		case MAGNIFY: // 10%拡大
			ve.mag *= 1.1;
			break;
		case SHRINK: // 10%縮小
			ve.mag *= 0.9;
			break;
		case X2: // 2倍に拡大
			ve.mag *= 2.0;
			break;
		case FIT_WIDTH: // 幅をフィット
			ve.mag = (double) d.width / (double) w;
			break;
		case FIT_HEIGHT: // 高さをフィット
			ve.mag = (double) d.height / (double) h;
			break;
		}
		// 現在のスケーリング画像を消去する
		// ve.simg.flush();
		// ve.simg = null;
		try {
			picQueue.put(ve);
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	private void handleKey(KeyEvent e) {
		char c = e.getKeyChar();
		if (e.isAltDown()) {
			changeSession(c);
			return;
		}
		
		switch (c) {
		case ' ':
			WtView.next();
			break;
		case 'q':
			WtView.quit();
			break;
		case '1':
			showScaledImage(Mag.X1); // 等倍表示
			break;
		case '2':
			showScaledImage(Mag.X2); // 2倍表示
			break;
		case 'w':
			showScaledImage(Mag.FIT_WIDTH); // 幅をフィット
			break;
		case 'h':
			showScaledImage(Mag.FIT_HEIGHT); // 高さをフィット
			break;
		case '-':
			showScaledImage(Mag.SHRINK); // 縮小する
			break;
		case '^':
			showScaledImage(Mag.MAGNIFY); // 拡大する
			break;
		case 'u':
			ve.up++; // ランキングアップリクエスト
			break;
		case 'd':
			ve.down++; // ランキングダウンリクエスト
			break;
		case 'x':
			WtView.ss.extraSpecified(); // 画像をエキストラ指定する
			break;
		case 's':
			//WtView.ss.writeScaledPic(); // スケーリングされた画像を保存する
			break;
		case 'p':
			WtView.ss.pause(); // 計時を中断する
			break;
		case 'b':
			WtView.ss.breakPause(); // 計時を再開する
			break;
		default:
		}
	}

	/**
	 * セッションの切り替え。Altキーが押された状態で呼び出される
	 * @param c タイプされた数値キー。eキーならエキストラモードのトグル。0ならゼロモードのトグル
 	 */
	private void changeSession(char c) {
		int i = 0;
		switch (c) {
		case '1': i=0; break;
		case '2': i=1; break;
		case '3': i=2; break;
		case '4': i=3; break;
		case '5': i=4; break;
		case '6': i=5; break;
		case '7': i=6; break;
		case '8': i=7; break;
		case '9': i=8; break;
		case 'z': i=0; break;
		case 'x': i=1; break;
		case 'c': i=2; break;
		case 'v': i=3; break;
		case 'b': i=4; break;
		case 'n': i=5; break;
		case 'm': i=6; break;
		case ',': i=7; break;
		case '.': i=8; break;
		case 'e': i=-1; break;
		case '0': i=-2; break;
		default: return;
		}
		WtView.changeSession(i);
	}
	
	private class ImgPanel extends JPanel {
		private Dimension d = new Dimension();

		ImgPanel() {
			super(new BorderLayout());
		}

		public Dimension getPreferredSize() {
			if (img == null)
				return getExtentSize();
			d.width = img.getWidth(this);
			d.height = img.getHeight(this);
			return d;
		}

		public void paint(Graphics g) {
			if (img == null)
				return;
			super.paint(g);
			int w = img.getWidth(this);
			int h = img.getHeight(this);
			Dimension d = getExtentSize();
			int x = (d.width > w) ? (d.width - w) / 2 : 0;
			int y = (d.height > h) ? (d.height - h) / 2 : 0;
			g.drawImage(img, x, y, this);
		}
	}

    Point getPosition() {
        return getViewport().getViewPosition();
    }
    
    void setPosition(final Point p) {
        EventQueue.invokeLater(new Runnable() {
			public void run() {
                getViewport().setViewPosition(p);
			}
		});
    }
}
