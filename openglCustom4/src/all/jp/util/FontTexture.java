package all.jp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Bitmap;  
import android.graphics.Bitmap.Config;  
import android.graphics.Canvas;  
import android.graphics.Paint;  
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.opengl.GLUtils;
import android.util.Log;
import android.graphics.Paint.FontMetrics;

import javax.microedition.khronos.opengles.GL10;

public class FontTexture {
	 private static final int FONT_MAX=256;
	 
	 public class TextureStruct{
		 int width;
		 int height;
		 int id;
		 Bitmap image;
	 };
	 
	 private TextureStruct m_texture;
	 private Canvas m_canvas;
	 public Paint m_paint;

	 //�ꖇ�̑傫�ȃe�N�X�`���Ƀt�H���g�������Ă����̂ł��̕`��ʒu
	 private int m_pre_draw_offset;
	 private int m_pre_draw_write_cnt;

	 private int m_size_x[]=new int[FONT_MAX];
	 private int m_size_y[]=new int[FONT_MAX];
	 
	 //�ǂݍ��݈ʒu	 
	 private int m_pre_draw_read_cnt;
	 private int m_pre_draw_read_offset;
	 
	 //�t�H���g�T�C�Y���
	 private int m_font_top_offset;
	 private int m_font_bottom_offset;

	 private boolean isBold = false;
	 public boolean calcIndexMode = false;
	 
	 public int fontSzie = 11;
	 public int maxLineCount = 4;
	 public int height_margin = 0;
	 
	 //���������m�ۂ���
	 public void createTextBuffer(GL10 gl)
	 {	
		 	//�ꖇ�̋���ȃe�N�X�`�����`����
	 		m_texture=new TextureStruct();
	 		m_texture.width=512;
	 		m_texture.height=256;
			m_texture.image = Bitmap.createBitmap(m_texture.width, m_texture.height, Config.ARGB_8888);  
			m_texture.id=0;
			
			//BMP�ւ̕`��R���e�L�X�g���擾����
			m_canvas=new Canvas(m_texture.image);
			m_paint =new Paint();
			
			//�t�H���g��`
		 	m_paint.setTextSize(fontSzie);
		 	m_paint.setAntiAlias(true);
		 	m_paint.setARGB(0xff, 0xFF, 0xFF, 0xFF);

		 	//�t�H���g�T�C�Y�̎擾
		 	FontMetrics fontMetrics = m_paint.getFontMetrics();
		 	m_font_top_offset = (int)Math.ceil(0 + fontMetrics.top);	//�x�[�X���C����s�N�Z��
		 	m_font_bottom_offset = (int)Math.ceil(0 + fontMetrics.bottom);	//�x�[�X���C�����s�N�Z��

		 	//�e�N�X�`���𐶐�����
		 	alocTexture(gl);
	 }
	 
	 private void alocTexture(GL10 gl)
	 {
			//�e�N�X�`���𐶐�����
		 	int textures[]=new int[1];
		 	gl.glGenTextures(1, textures, 0);
		 	gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		 	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		 	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		 	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		 	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		 	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, m_texture.image, 0);
			m_texture.id=textures[0];
	 }

	 private void releaseTexture(GL10 gl)
	 {
		 	if(m_texture.id!=0){
		 		int []id=new int[1];
		 		id[0]=m_texture.id;
		 		gl.glDeleteTextures(1, id, 0);
		 		m_texture.id=0;
		 	}
	 }
	 
	 //�e�N�X�`���𕜌�����
	 public void onResume(GL10 gl)
	 {
		 releaseTexture(gl);
		 alocTexture(gl);		 
	 }

	 //���������J������
	 public void onDestroy(GL10 gl)
	 {
		 releaseTexture(gl);
	 }

	 //1�t���[���ŕK�v�ȕ�����S�Đ�s���ĕ`�悵�Ă���
	 public void preDrawBegin()
	 {
		//�������݈ʒu������
	 	m_pre_draw_offset=0;
	 	m_pre_draw_write_cnt=0;
	 	
	 	//�ǂݍ��݈ʒu������
	 	m_pre_draw_read_cnt=0;
	 	m_pre_draw_read_offset=0;

	 	isBold = false;
	 	
	 	//�Ƃ肠�������������Ă���
	 	m_texture.image.eraseColor(0);
	 }
	 
	 public void resetPreDrawCount()
	 {
	 	//�ǂݍ��݈ʒu������
	 	m_pre_draw_read_cnt=0;
	 	m_pre_draw_read_offset=0;
	 }
	 
	 public int drawStringToTexture(String text,int sx)
	 {
		 return drawStringToTexture(text,sx, 0, 0);
	 }
	 
	 public int drawStringToTexture(String text,int sx, int string_draw_index, int drawCharCount)
	 {	
	 	if(m_pre_draw_write_cnt>=FONT_MAX)
	 		return string_draw_index;
	 	int y=m_pre_draw_offset;
	 	
	 	//����`�悵���t�H���g�̗݌v���Ɨ݌v����
	 	int height=0;
	 	int width=0;

	 	//����sx�Ő܂�Ԃ���BMP�ɕ`��
	 	int lineCount = 0;
	 	int line_end_index = 1;
	 	while(line_end_index!=0 && lineCount < maxLineCount){
	 		String mesureString;
	 		if(drawCharCount != 0)
	 			mesureString = text.substring(string_draw_index, drawCharCount);
	 		else
	 			mesureString = text.substring(string_draw_index);
	 		
	 		line_end_index=m_paint.breakText(mesureString, true, sx, null);	//�܂�Ԃ��͎����ł��K�v
	 		
	 		if(mesureString.indexOf("\n") > 0 && mesureString.indexOf("\n")< line_end_index)
	 		{
	 			line_end_index = mesureString.indexOf("\n") + 1;
	 		}

	 		
	 		if(line_end_index!=0){
	 			String line = text.substring(string_draw_index, string_draw_index + line_end_index);
	 			
	 			//drawText�̓x�[�X���C���w��
	 			//�g�b�v���C������V��̈ʒu���v�Z���ĕ`�悷��
	 			int line_height=(-m_font_top_offset+m_font_bottom_offset);
	 			line_height += height_margin;
	 			int from_y=y+height-m_font_top_offset;
	 			
	 			//�`��悪�I�[�o�t���[����ꍇ�͕`�悵�Ȃ�
	 			if(y+line_height>=m_texture.height)
	 				return string_draw_index;
	 			
	 			//�`���
	 			line = line.replaceAll("\\n", "");
	 			
	 			if(!calcIndexMode)
	 				m_canvas.drawText(line, 0, from_y, m_paint);
	 			
	 			//1���C���̕����v�Z����
	 			int line_width=(int)m_paint.measureText(line);
	 			if(width<line_width){
	 				width=line_width;
	 			}
	 			
	 			//���̍s��
	 			lineCount++;
	 			height += line_height;
	 			string_draw_index += line_end_index;
	 		}
	 	}
	 		 	
	 	//����`�悵�����̂̃T�C�Y��o�^���Ă���
	 	m_size_x[m_pre_draw_write_cnt]=width;
	 	m_size_y[m_pre_draw_write_cnt]=height;
	 	m_pre_draw_offset+=height;
	 	m_pre_draw_write_cnt++;
	 	
	 	//System.out.println("CharCount = " + text.length());
	 	//System.out.println("string_draw_index = " + string_draw_index);
	 	
	 	if(text.length() == string_draw_index)
	 		return -1;
	 	else
	 		return string_draw_index;
	 }

	 public void preDrawEnd(GL10 gl)
	 {
		 //�e�N�X�`�����X�V����
		 int textures[]=new int[1];
		 textures[0]=m_texture.id;
		 gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);		 
//		 long start = System.currentTimeMillis();
		 GLUtils.texSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, m_texture.image);
//		 long end = System.currentTimeMillis();
//		 Log.i("",""+(end-start)+"ms");
	 }

	 //2�p�X�ڂɏ��Ԃɕ������擾����UV���W��ύX���Ȃ��當����`�悷��
	 public int getTexture(){
	 	return m_texture.id;
	 }

	 public int getWidth(){
		if(m_pre_draw_read_cnt>=m_pre_draw_write_cnt)
			return 1;
	 	return m_size_x[m_pre_draw_read_cnt];
	 }

	 public int getHeight(){
		if(m_pre_draw_read_cnt>=m_pre_draw_write_cnt)
			return 1;
		 return m_size_y[m_pre_draw_read_cnt];
	 }

	 public int getOffset(){
		if(m_pre_draw_read_cnt>=m_pre_draw_write_cnt)
			return 0;
		 return m_pre_draw_read_offset;
	 }
	 
	 public void nextReadPoint(){
		m_pre_draw_read_offset+=m_size_y[m_pre_draw_read_cnt];
	 	m_pre_draw_read_cnt++;
	 	if(m_pre_draw_read_cnt>=FONT_MAX)
			m_pre_draw_read_cnt=FONT_MAX-1;
	 }
}
