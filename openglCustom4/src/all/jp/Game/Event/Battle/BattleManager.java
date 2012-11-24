package all.jp.Game.Event.Battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


import javax.microedition.khronos.opengles.GL11;

import cx.fam.tak0294.storage.DBHelper;

import all.jp.Game.GameMode;
import all.jp.Game.Base.GameManager;
import all.jp.Game.Character.CharacterBase;
import all.jp.Game.Character.Enemy;
import all.jp.Game.Character.Player;
import all.jp.Game.Item.ItemBase;
import all.jp.Game.Item.ItemType;
import all.jp.Game.Item.Weapon.WeaponType;
import all.jp.Game.Magic.MagicBase;
import all.jp.Game.Magic.MagicType;
import all.jp.util.FontTexture;
import all.jp.util.Global;
import all.jp.util.GraphicUtil;

public class BattleManager
{

	
	private GameManager game;
	private BattleAnimation animation;
	
	private int MODE;
	private int mainWindowTexture;
	private int subWindowTexture;
	private int mainMenuTexture;
	private int fightButtonTexture;
	private int guardButtonTexture;
	private int fightTargetOkButtonTexture;
	private int itemButtonTexture;
	private int menuUpButtonTexture;
	private int menuRightButtonTexture;
	private int magicButtonTexture;
	private int backButtonTexture;
	private int escapeButtonTexture;
	private FontTexture fontTexture;

	
	private boolean isCursorEnd 	= false;
	private int currentLineIndex  	= 0;
	private int currentCharIndex    = 0;
	private ArrayList<Integer> lineIndexes;
	private String currentEventText;
	private boolean nextPageRequest = false;
	private boolean skipPageRequest = false;
	
	//���͎�t��~�E�F�C�g.
	private int inputWait = 0;
	
	//�G���J�E���g�A�j���[�V�����p.
	private float flashAlpha = 0.8f;
	
	//���݃G���J�E���g���̓G.
	private ArrayList<HashMap<String, String>> activeEnemiesArray;
	private ArrayList<Enemy> activeEnemies;
	private boolean requestTextureLoad = false;
	private int currentTargetEnemyIndex = 0;
	private int currentTargetPlayerIndex = 0;
	
	//�s����.
	private ArrayList<CharacterBase> turnList;
	private int currentTurnIndex = 0;
	private int currentGroupNumCount = 0;
	private int currentGroupNum      = 0;
	private int whatToDoDecidedCount = 0;
	
	//�퓬�I���t���O.
	private boolean isBattleEnded = false;
	private boolean isReleasedTexture = false;
	private boolean isRunAway = false;
	private boolean isGameOver = false;
	
	//�l���o���l�A�S�[���h.
	private int resultExp;
	private int resultGold;

	//���j���[�ʒu����.
	private float menuYPos = 0.7f;
	
	//====================================================
	//	�R���X�g���N�^.
	//====================================================
	public BattleManager(GameManager game)
	{
		this.game = game;
		MODE = BattleMode.Waiting;
		this.animation = new BattleAnimation(game);
		this.fontTexture = new FontTexture();
		this.fontTexture.maxLineCount = 3;

	}
	
	public void setMode(int mode)
	{
		this.MODE = mode;
	}

	//====================================================
	//	�e�N�X�`��������.
	//====================================================
	public void initTextures(GL11 gl)
	{
		this.fontTexture.createTextBuffer(gl);
		mainWindowTexture  = GraphicUtil.loadTexture(gl, "battlemainwindow");
		subWindowTexture   = GraphicUtil.loadTexture(gl, "battlesubwindow");
		mainMenuTexture    = GraphicUtil.loadTexture(gl, "battlemainmenu");
		fightButtonTexture = GraphicUtil.loadTexture(gl, "fight_button");
		guardButtonTexture = GraphicUtil.loadTexture(gl, "guard_button");
		itemButtonTexture  = GraphicUtil.loadTexture(gl, "item_button");
		menuUpButtonTexture = GraphicUtil.loadTexture(gl, "menu_up_button");
		menuRightButtonTexture = GraphicUtil.loadTexture(gl, "menu_right_button");
		escapeButtonTexture    = GraphicUtil.loadTexture(gl, "escape_button"); 
		
		fightTargetOkButtonTexture = GraphicUtil.loadTexture(gl, "fight_target_ok_button");
		magicButtonTexture = GraphicUtil.loadTexture(gl, "magic_button");
		backButtonTexture = GraphicUtil.loadTexture(gl, "back_button");
		GraphicUtil.loadTexture(gl, "battlemenuindex");
	}
	
	//========================================
	//	�������������s��.
	//========================================
	private void init()
	{
		activeEnemiesArray	= new ArrayList<HashMap<String, String>>();
		activeEnemies		= new ArrayList<Enemy>();
		requestTextureLoad  = false;
		currentTurnIndex = 0;
		currentGroupNumCount = 0;
		currentGroupNum		 = 0;
		isBattleEnded		 = false;
		isReleasedTexture	 = false;
		isRunAway			 = false;
		isGameOver			 = false;
		inputWait			 = 0;
		flashAlpha = 0.8f;
		resultExp = 0;
		resultGold = 0;
		currentTargetEnemyIndex = 0;
		currentTargetPlayerIndex = 0;
	}
	
	public void setupBattle(String enemy_codes)
	{
		//	������.
		this.init();
		
		//���O�v���[�g�ړ�.
		game.activeParty.setNamePlateBattlePosition();
		
		//========================================
		//	DB����G�擾.
		//========================================
		//enemy_codes = 1|2|3
		DBHelper db = new DBHelper(game.activity);
		String[] codes_array = enemy_codes.split(";");
		for(int ii=0;ii<codes_array.length;ii++)
		{
			HashMap<String,String> in = new HashMap<String,String>();
			in.put("chara_code", codes_array[ii]);
			HashMap<String, String> res = db.get("enemy_mt", in).get(0); 
			activeEnemiesArray.add(res);
		}

		//�e�N�X�`���ǂݍ��݃t���OOn.
		requestTextureLoad = true;

		//========================================
		//	�p�[�e�B�̈�l�ڂ��A�N�e�B�u�ɂ���.
		//========================================
		game.activeParty.activePlayerIndex = 0;
		whatToDoDecidedCount = 0;

		
		MODE = BattleMode.EncountAnime1;
		
		//this.srcRowIndex = -1;
		//this.readNextEvent();
		//this.nextTurn();
	}

	
	//====================================================
	//	�v���C���[�ɍs����I��������.
	//====================================================
	private void askWhatToDo()
	{
		//====================================================
		//	�S���̍s�������肵����퓬�J�n.
		//====================================================
		if(game.activeParty.activePlayerIndex >= game.activeParty.members.size())
		{
			this.decideTurn();
			return;
		}
		
		CharacterBase currentCharacter = game.activeParty.members.get(game.activeParty.activePlayerIndex);
		
		//����ł����΂�.
		if(!currentCharacter.isAlive)
		{
			game.activeParty.activePlayerIndex++;
			this.askWhatToDo();
		}
		else
		{
			MODE = BattleMode.WaitingWhatTodo;
			this.setTextAndShow(currentCharacter.name + "�͂ǂ�����H");
		}
		
	}
	
	
	
	//====================================================
	//	�v���C���[�ɃA�C�e����I��������.
	//====================================================
	private void askWhatItem()
	{
		game.item.init();
		MODE = BattleMode.WaitingItemSelect;
		this.setTextAndShow("");
	}


	//====================================================
	//	�v���C���[�ɖ��@��I��������.
	//====================================================
	private void askWhatMagic()
	{
		game.magic.init();
		MODE = BattleMode.WaitingMagicSelect;
		this.setTextAndShow("");
	}

	
	//====================================================
	//	���Ԃ����߂�.
	//====================================================
	private void decideTurn()
	{
		//���ԃ��X�g�z���������.
		this.turnList = new ArrayList<CharacterBase>();
		this.currentTurnIndex = -1;
		this.currentGroupNumCount = 1;
		this.currentGroupNum = 0;

		for(int ii=0;ii<game.activeParty.members.size();ii++)
		{
			if(game.activeParty.members.get(ii).isAlive)
				turnList.add(game.activeParty.members.get(ii));
		}
		
		for(int ii=0;ii<this.activeEnemies.size();ii++)
		{
			//TODO.
			//�����ƌv�Z����.
			//�G�̍s�������肵�Ă���.
			if(this.activeEnemies.get(ii).isAlive)
			{
				this.activeEnemies.get(ii).decideBattleAct();
				turnList.add(this.activeEnemies.get(ii));
			}
		}
		
		//���בւ�.
		Collections.sort(turnList, new Comparator<CharacterBase>(){
			public int compare(CharacterBase c1, CharacterBase c2) {
				return c2.dex - c1.dex;
			}
		});
		
		this.nextTurn();
		
		System.out.println("decideTurn");
	}

	//====================================================
	//	���̃^�[�������s����.
	//====================================================
	private void nextTurn()
	{
		if(this.currentGroupNumCount < this.currentGroupNum)
		{
			this.currentGroupNumCount++;
		}
		else
		{
			this.currentTurnIndex++;
			this.currentGroupNumCount = 1;
		}
		
		if(this.currentTurnIndex == this.turnList.size())
		{
			System.out.println("�S���^�[���I��");
			resetTurn();
			return;
		}
		
		CharacterBase currentCharacter = this.turnList.get(this.currentTurnIndex);
		CharacterBase targetCharacter  = (CharacterBase) this.turnList.get(this.currentTurnIndex).Target;

		//���S���Ă����玟�։�.
		if(!currentCharacter.isAlive)
		{
			nextTurn();
			return;
		}

		
		this.currentGroupNum = currentCharacter.groupNum;
		
		
		//------------------------------------------
		//	�^�[���C�����̃��b�Z�[�W�\��.
		//------------------------------------------
		if(currentCharacter.BattleActType == BattleActType.Fight)
		{
			this.setTextAndShow(currentCharacter.name + "�̍U���I");
		}			
		else if(currentCharacter.BattleActType == BattleActType.Guard)
		{
			this.resultTurn();
			return;
		}
		else if(currentCharacter.BattleActType == BattleActType.Item)
		{
			ItemBase item = (ItemBase) currentCharacter.Use;
			this.setTextAndShow(currentCharacter.name + "��" + targetCharacter.name + "��" + item.name + "���g�����I");
		}
		else if(currentCharacter.BattleActType == BattleActType.Magic)
		{
			MagicBase magic = (MagicBase) currentCharacter.Use;
			this.setTextAndShow(currentCharacter.name + "��" + targetCharacter.name + "��" + magic.name + "���g�����I");
		}
		else if(currentCharacter.BattleActType == BattleActType.Run)
		{
			this.resultTurn();
			return;
		}

		//�퓬�^�[���J�n.
		MODE = BattleMode.EnteringTurn;
		
	}


	//====================================================
	//	�^�[���̎��s����.
	//====================================================
	private void resultTurn()
	{
		CharacterBase currentCharacter = this.turnList.get(this.currentTurnIndex);
		CharacterBase targetCharacter  = (CharacterBase) this.turnList.get(this.currentTurnIndex).Target;
		String resultText = "";
		
		//===================================================================================
		//	�s��.
		//		����.
		//===================================================================================
		if(currentCharacter.BattleActType == BattleActType.Run)
		{
			//TODO
			//�m���v�Z���s��
			boolean isRunSuccess = false;
			resultText += currentCharacter.name + "�͓����悤�Ƃ���\n";

			int rand = Global.rand.nextInt(100);
			if(rand < 70)
				isRunSuccess = true;
			
			if(isRunSuccess)
			{
				resultText += "���������I\n";
				isRunAway 		= true;
				isBattleEnded 	= true;
			}
			else
			{
				resultText += "��肱�܂ꂽ�I\n";
			}
		}
		//===================================================================================
		//	�s��.
		//		�키.
		//===================================================================================
		else if(currentCharacter.BattleActType == BattleActType.Fight)
		{

			//=========================================================
			//	�s���̑Ώۂ�����ł���.
			//=========================================================
			if(!targetCharacter.isAlive)
			{
				//====================================================
				//	�U���^�[�Q�b�g��Enemy.
				//====================================================
				if(targetCharacter.type.equals("enemy"))
				{
					//TODO.
					//Patch	�����Ă���^�[�Q�b�g���擾.
					for(int ii=0;ii<this.activeEnemies.size();ii++)
					{
						if(this.activeEnemies.get(ii).isAlive)
							targetCharacter = this.activeEnemies.get(ii);
					}
				}
				//====================================================
				//	�U���^�[�Q�b�g��Player.
				//====================================================
				else
				{
					resultText = "������"+targetCharacter.name + "�͎���ł���I\n";
				}
			}
			
			//=========================================================
			//	�s���̑Ώۂ������Ă���.
			//=========================================================
			if(targetCharacter.isAlive)
			{
				int damage = BattleDamageCalculator.getDq3Damage(currentCharacter, targetCharacter);
				
				resultText += targetCharacter.name + "��" + damage + "�̃_���[�W�I\n";
				
				//	HP�����炷.
				targetCharacter.hp -= damage;
				
				//====================================================
				//	�^�[�Q�b�g��Enemy.
				//====================================================
				if(targetCharacter.type.equals("enemy"))
				{
					
					this.animation.setWeaponEffect(currentCharacter.getWeaponType(), targetCharacter);
					targetCharacter.isHitAnimation = true;
				}
				//====================================================
				//	�^�[�Q�b�g��Player.
				//====================================================
				else if(targetCharacter.type.equals("player"))
				{
					targetCharacter.isHitAnimation = true;
					targetCharacter.setUpdateTexture();
				}
				
			}
		}
		//===================================================================================
		//	�s��.
		//		�g�����.
		//===================================================================================
		else if(currentCharacter.BattleActType == BattleActType.Guard)
		{
			resultText = currentCharacter.name + "�͐g������Ă���I";
		}
		//===================================================================================
		//	�s��.
		//		�A�C�e�����g��.
		//===================================================================================
		else if(currentCharacter.BattleActType == BattleActType.Item)
		{
			//�g�p����A�C�e��.
			ItemBase item = (ItemBase) currentCharacter.Use;
			
			
			boolean isUsed = true;
			
			//���S���͎g���Ȃ�.
			if(!targetCharacter.isAlive)
			{
				resultText += targetCharacter.name + "�͎���ł���I";
				isUsed = false;
			}
			else
			{
				if(item.hp > 0 && targetCharacter.hp < targetCharacter.maxHp)
				{
					int healAmount = Global.rand.nextInt(item.hp/2) + item.hp/2;
					targetCharacter.healHp(healAmount);
					resultText += targetCharacter.name + "��HP��" + healAmount + "�񕜁I\n";
				}
				if(item.mp > 0 && targetCharacter.mp < targetCharacter.maxMp)
				{
					int healAmount = Global.rand.nextInt(item.mp/2) + item.mp/2;
					targetCharacter.healMp(healAmount);
					resultText += targetCharacter.name + "��MP��" + healAmount + "�񕜁I\n";
				}
				
				if(item.def > 0)
				{
					targetCharacter.addBuff(item);
					resultText += targetCharacter.name + "�͖h��͂��オ�����I\n";
				}
				if(item.str > 0)
				{
					targetCharacter.addBuff(item);
					resultText += targetCharacter.name + "�͍U���͂��オ�����I\n";
				}
				if(item.dex > 0)
				{
					targetCharacter.addBuff(item);
					resultText += targetCharacter.name + "�͑f�������オ�����I\n";
				}
			}

			//�A�C�e�����炷.
			if(isUsed)
			{
				item.stock--;
				if(item.stock == 0)
				{
					game.activeParty.items.remove(item);
				}
			}
			
			if(resultText.equals(""))
				resultText = "���ʂ͂Ȃ�����";
			else
				targetCharacter.setUpdateTexture();
		}
		//===================================================================================
		//	�s��.
		//		���@���g��.
		//===================================================================================
		else if(currentCharacter.BattleActType == BattleActType.Magic)
		{
			//=========================================================
			//	�s���̑Ώۂ�����ł���.
			//=========================================================
			if(!targetCharacter.isAlive)
			{
				if(targetCharacter.type.equals("enemy"))
				{
					//Patch	�����Ă���^�[�Q�b�g���擾.
					for(int ii=0;ii<this.activeEnemies.size();ii++)
					{
						if(this.activeEnemies.get(ii).isAlive)
							targetCharacter = this.activeEnemies.get(ii);
					}
				}
				else
				{
					resultText = "������"+targetCharacter.name + "�͎���ł���I\n";
				}
			}
			
			//�g�p���閂�@.
			MagicBase magic = (MagicBase) currentCharacter.Use;
			currentCharacter.mp -= magic.mp;
			
			//�^�[�Q�b�g��Enemy.
			if(targetCharacter.type.equals("enemy"))
			{
				animation.setAttackMagicEffect(magic.effectType, targetCharacter);
				
				//�_���[�W�ʌv�Z.
				int damage = BattleDamageCalculator.getDq3MagicDamage(magic, targetCharacter);

				resultText += targetCharacter.name + "��" + damage + "�̃_���[�W�I\n";
				targetCharacter.isHitAnimation = true;
				
				//	HP�����炷.
				targetCharacter.hp -= damage;
				
			}
			//�^�[�Q�b�g��Player.
			else
			{
				animation.setCureMagicEffect(magic.effectType, targetCharacter);
				
				if(magic.cureHp > 0 && targetCharacter.hp < targetCharacter.maxHp)
				{
					int healAmount = Global.rand.nextInt(magic.cureHp/2) + magic.cureHp/2;
					targetCharacter.healHp(healAmount);
					resultText += targetCharacter.name + "��HP��" + healAmount + "�񕜁I\n";
				}
				if(magic.cureMp > 0 && targetCharacter.mp < targetCharacter.maxMp)
				{
					int healAmount = Global.rand.nextInt(magic.cureMp/2) + magic.cureMp/2;
					targetCharacter.healMp(healAmount);
					resultText += targetCharacter.name + "��MP��" + healAmount + "�񕜁I\n";
				}
				
				if(magic.def > 0)
				{
					targetCharacter.addMagicBuff(magic);
					resultText += targetCharacter.name + "�͖h��͂��オ�����I\n";
				}
				if(magic.str > 0)
				{
					targetCharacter.addMagicBuff(magic);
					resultText += targetCharacter.name + "�͍U���͂��オ�����I\n";
				}
				if(magic.dex > 0)
				{
					targetCharacter.addMagicBuff(magic);
					resultText += targetCharacter.name + "�͑f�������オ�����I\n";
				}
				
				if(resultText.equals(""))
					resultText = "���ʂ͂Ȃ�����";
			}
			
			currentCharacter.setUpdateTexture();
			targetCharacter.setUpdateTexture();
		}


		
		if(currentCharacter.BattleActType != BattleActType.Guard &&
		   currentCharacter.BattleActType != BattleActType.Run)
		{
			//===================================================================================
			//	���S�m�F.
			//===================================================================================
			if(targetCharacter.type.equals("enemy"))
			{
				//����.
				if(targetCharacter.hp <= 0)
				{
					resultText += targetCharacter.name + "�͎���\n";
					resultGold += Global.rand.nextInt(targetCharacter.gold) + 1;

					//�X�e�[�^�X�����Z�b�g����.
					targetCharacter.hp = targetCharacter.maxHp;
					targetCharacter.mp = targetCharacter.maxMp;
					targetCharacter.groupNum--;
					targetCharacter.setUpdateTexture();

					//�O���[�v�����O = ���̓G�O���[�v�̏���.
					if(targetCharacter.groupNum == 0)
					{
						targetCharacter.isAlive = false;

						//�S�ẴO���[�v��|�����H
						int activeCnt = 0;
						for(int ii=0;ii<this.activeEnemies.size();ii++)
						{
							if(this.activeEnemies.get(ii).isAlive)
								activeCnt++;
						}

						//�퓬�I��.
						if(activeCnt == 0)
						{
							isBattleEnded = true;
							resultText += "�����X�^�[�͑S�ł���\n";
							resultText += "���ꂼ��" + resultExp + "�̌o���l����ɓ��ꂽ\n";
							resultText += resultGold + "Gold����ɓ��ꂽ\n";
							game.activeParty.gold += resultGold;
							resultText += this.makeEndBattleString(resultExp);
						}
					}
				}
			}
			//===================================================================================
			//	�^�[�Q�b�g��Player.
			//===================================================================================
			else if(targetCharacter.type.equals("player"))
			{
				//����.
				if(targetCharacter.isAlive && targetCharacter.hp <= 0)
				{
					resultText += targetCharacter.name + "�͎���\n";
					targetCharacter.hp = 0;
					targetCharacter.isAlive = false;
					
					//�S�Ŕ���.
					int aliveCount = 0;
					for(int ii=0;ii<game.activeParty.members.size();ii++)
					{
						if(game.activeParty.members.get(ii).isAlive)
							aliveCount++;
					}
					if(aliveCount == 0)
					{
						resultText += "�p�[�e�B�͑S�ł���";
						isRunAway 		= true;
						isBattleEnded 	= true;
						isGameOver		= true;
					}
				}

				targetCharacter.setUpdateTexture();
			}
		}
		
		this.setTextAndShow(resultText);
		
		//�퓬�^�[������.
		if(isBattleEnded)
			MODE = BattleMode.Text;
		else
			MODE = BattleMode.ResultTurn;
	}
	

	//====================================================
	//	�^�[���̏I����.
	//====================================================
	private void resetTurn()
	{
		for(int ii=0;ii<game.activeParty.members.size();ii++)
		{
			game.activeParty.members.get(ii).guardDef = 0;		//�h�䒆�̉���.
			game.activeParty.members.get(ii).reduceBuffTurn();	//�o�t�̎c��^�[���������炷.
		}
		
		game.activeParty.activePlayerIndex = 0;
		this.askWhatToDo();
	}
	
	//====================================================
	//	�v���C���[�ɓG��I��������.
	//====================================================
	private void askAttackTarget()
	{
		MODE = BattleMode.WaitingAttackTargetSelect;
		this.setTextAndShow("�^�[�Q�b�g��I�����Ă��������B");
	}

	//====================================================
	//	�v���C���[�ɃA�C�e���̎g�p�Ώۂ�I�΂���.
	//====================================================
	private void askUseTarget()
	{
		MODE = BattleMode.WaitingItemTargetSelect;
		this.setTextAndShow("�A�C�e�����g���Ώۂ�I�����Ă��������B");
	}

	//====================================================
	//	�v���C���[�ɖ��@�̎g�p�Ώۂ�I�΂���.
	//====================================================
	private void askMagicTarget()
	{
		MODE = BattleMode.WaitingMagicTargetSelect;
		this.setTextAndShow("���@���g���Ώۂ�I�����Ă��������B");
	}

	
	
	//------------------------------------------------------
	//	�e�L�X�g��ݒ肵�A�\������.
	//------------------------------------------------------
	public void setTextAndShow(String text)
	{
		//�s�̐܂�Ԃ�Index�z��.
		this.isCursorEnd = false;
		this.currentLineIndex = 0;
		this.currentCharIndex = 0;
		this.lineIndexes = new ArrayList<Integer>();
		this.currentEventText = text;
		
		this.fontTexture.calcIndexMode = true;
		int index = 0;
		while(true)
		{
			index = this.fontTexture.drawStringToTexture(text, 400, index, 0);
			if(index != -1)
				this.lineIndexes.add(index);
			else
				break;
		}
		this.lineIndexes.add(text.length()-1);

		this.fontTexture.calcIndexMode = false;
		this.fontTexture.resetPreDrawCount();
	}

	public void nextPage()
	{
		this.isCursorEnd = false;
		this.currentCharIndex = this.lineIndexes.get(this.currentLineIndex);
		this.currentLineIndex++;
		if(this.currentLineIndex > this.lineIndexes.size())
			this.currentLineIndex = this.lineIndexes.size();
	}

	private int getPreviousCharIndex(int lineIndex)
	{
		if(lineIndex == 0)
			return 0;
		else
		{
			return this.lineIndexes.get(lineIndex-1);
		}
	}


	
	//=================================================================================================
	//
	//	�`��֐�.
	//
	//
	//=================================================================================================
	public void draw(GL11 gl)
	{
		//------------------------------------------------------------
		//	�G���J�E���g���̓G.
		//------------------------------------------------------------
		if(requestTextureLoad)
		{
			for(int ii=0;ii<this.activeEnemiesArray.size();ii++)
			{
				Enemy en = new Enemy(game, activeEnemiesArray.get(ii));
				en.initTexture(gl, this.activeEnemiesArray.get(ii).get("chara_imageName"));
				//TODO
				//x�̌v�Z����ł����̂�?
				float width = 0.6f;
				if(this.activeEnemiesArray.size() == 4)
					width = 0.5f;
				en.x = (ii*width) - ((width*this.activeEnemiesArray.size()))*0.5f;
				this.activeEnemies.add(en);
				
				//�l���o���l�Ȃǂ̐ݒ�.
				resultExp += en.exp * en.groupNum;
			}
			
			requestTextureLoad = false;
		}
		
		//------------------------------------------------------------
		//	�G���J�E���g�A�j���[�V������.
		//------------------------------------------------------------
		if(MODE == BattleMode.EncountAnime1)
		{
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GraphicUtil.drawRectangle(gl, 0.0f, 0.0f, 3.0f, 3.0f, 1.0f, 1.0f, 1.0f, flashAlpha);
			gl.glDisable(GL11.GL_BLEND);
			
			flashAlpha -= 0.05f;
			
			if(flashAlpha < 0)
			{
				flashAlpha = 0.0f;
				this.askWhatToDo();
			}
			
			game.renderer.particleSystem.addSparks(0.0f, 0.0f, 0.0f, 0.05f, 0.09f, 5, 0);
			
			return;
		}
		
		gl.glEnable(GL11.GL_BLEND);
		gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//�E�B���h�E�g�̕`��.
		GraphicUtil.drawTexture(gl, -0.3f, 0.2f, 2.3f, 1.5f, mainWindowTexture, 1.0f, 1.0f, 1.0f, 0.5f);
		GraphicUtil.drawTexture(gl, -0.3f, -0.8f, 2.3f, 0.35f, subWindowTexture, 1.0f, 1.0f, 1.0f, 1.0f);
	
		for(int ii=0;ii<this.activeEnemies.size();ii++)
		{
			this.activeEnemies.get(ii).draw(gl);
		}

		if(inputWait > 0)
		{
			inputWait--;
			gl.glDisable(GL11.GL_BLEND);
			return;
		}
		
		//���j���[�g�̕`��.
		if(MODE == BattleMode.WaitingWhatTodo ||
		   MODE == BattleMode.WaitingAttackTargetSelect ||
		   MODE == BattleMode.WaitingItemSelect ||
		   MODE == BattleMode.WaitingItemTargetSelect ||
		   MODE == BattleMode.WaitingMagicSelect ||
		   MODE == BattleMode.WaitingMagicTargetSelect)
		{
			GraphicUtil.drawTexture(gl, 1.19f, -0.53f + menuYPos, 0.5f, 1.55f, mainMenuTexture, 1.0f, 1.0f, 1.0f, 0.7f);
			
			if(MODE == BattleMode.WaitingWhatTodo)
			{
				GraphicUtil.drawTexture(gl, 1.19f, 0.05f + menuYPos, 0.4f, 0.2f, fightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.25f + menuYPos, 0.4f, 0.2f, guardButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.55f + menuYPos, 0.4f, 0.2f, itemButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				if(game.activeParty.getActivePlayer() != null && 
				   game.activeParty.getActivePlayer().magics.size() > 0)
					GraphicUtil.drawTexture(gl, 1.19f, -0.85f + menuYPos, 0.4f, 0.2f, magicButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				
				GraphicUtil.drawTexture(gl, 1.19f, -1.15f + menuYPos, 0.4f, 0.2f, escapeButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			}
			else if(MODE == BattleMode.WaitingAttackTargetSelect ||
					MODE == BattleMode.WaitingItemTargetSelect ||
					MODE == BattleMode.WaitingMagicTargetSelect)
			{
				GraphicUtil.drawTexture(gl, 1.19f, 0.05f + menuYPos, 0.4f, 0.2f, fightTargetOkButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.25f + menuYPos, 0.4f, 0.2f, menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.55f + menuYPos, -0.4f, 0.2f, menuRightButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.85f + menuYPos, 0.4f, 0.2f, backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			}
			else if(MODE == BattleMode.WaitingItemSelect ||
					MODE == BattleMode.WaitingMagicSelect)
			{
				GraphicUtil.drawTexture(gl, 1.19f, 0.05f + menuYPos, 0.4f, 0.2f, fightTargetOkButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.25f + menuYPos, 0.4f, 0.2f, menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.55f + menuYPos, 0.4f, -0.2f, menuUpButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
				GraphicUtil.drawTexture(gl, 1.19f, -0.85f + menuYPos, 0.4f, 0.2f, backButtonTexture, 1.0f, 1.0f, 1.0f, 1.0f);
			}
		}
		
		gl.glDisable(GL11.GL_BLEND);



		//------------------------------------------------------------
		//	�e�L�X�g�`�惂�[�h.
		//------------------------------------------------------------
		if(MODE == BattleMode.Text ||
		   MODE == BattleMode.WaitingWhatTodo ||
		   MODE == BattleMode.WaitingItemSelect ||
		   MODE == BattleMode.WaitingAttackTargetSelect ||
		   MODE == BattleMode.EnteringTurn ||
		   MODE == BattleMode.DoingTurn ||
		   MODE == BattleMode.ResultTurn)
		{
			if(this.skipPageRequest)
			{
				this.currentCharIndex = this.lineIndexes.get(currentLineIndex)-1;
				this.skipPageRequest = false; 
			}
			
			this.fontTexture.resetPreDrawCount();
			
			if(nextPageRequest)
			{
				this.nextPage();
				nextPageRequest = false;
			}

			if(this.currentLineIndex >= this.lineIndexes.size()-1)
				this.currentLineIndex = this.lineIndexes.size()-1;
			
			try{
				
				if(this.lineIndexes.size() > 0 &&
				   this.currentLineIndex < this.lineIndexes.size() &&
				   this.currentCharIndex <= this.lineIndexes.get(currentLineIndex))
				{
					this.fontTexture.preDrawBegin();
					this.currentCharIndex = this.fontTexture.drawStringToTexture(this.currentEventText, 400, this.getPreviousCharIndex(currentLineIndex), this.currentCharIndex+1);
					this.fontTexture.preDrawEnd(gl);
					
					if(this.currentCharIndex == this.lineIndexes.get(currentLineIndex))
						this.isCursorEnd = true;	
				
					
				}
			}catch(Exception e)
			{
				this.isCursorEnd = true;
				this.currentLineIndex = this.lineIndexes.size()-1;
			}
			
			int no	= fontTexture.getTexture();
			int sx	= fontTexture.getWidth();
			int sy	= fontTexture.getHeight();
			float offset= fontTexture.getOffset();
	
			gl.glEnable(GL11.GL_BLEND);
			gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GraphicUtil.drawTexture(gl,-(2.82f - sx/180.0f)*0.5f,(-1.3f - sy/140.0f) * 0.5f,sx/180.0f,sy/140.0f,no, 0,offset/256.0f,sx/512.0f, sy/256.0f, 1.0f,1.0f,1.0f, 1.0f);
			gl.glDisable(GL11.GL_BLEND);
		}
		
		
		//---------------------------------------------------------------------------------------
		//	�A�C�e���I����ʕ\��.
		//---------------------------------------------------------------------------------------
		if(MODE == BattleMode.WaitingItemSelect)
		{
			game.item.draw(gl);
		}
		
		//---------------------------------------------------------------------------------------
		//	���@�I����ʕ\��.
		//---------------------------------------------------------------------------------------
		if(MODE == BattleMode.WaitingMagicSelect)
		{
			game.magic.draw(gl);
		}
		
		
		//---------------------------------------------------------------------------------------
		//	�G�O���t�B�b�N�̃������J��.
		//---------------------------------------------------------------------------------------
		if(isBattleEnded && !isReleasedTexture)
		{
			int releasedCount = 0;
			for(int ii=0;ii<this.activeEnemies.size();ii++)
			{
				if(this.activeEnemies.get(ii).release(gl, isRunAway))
				{
					releasedCount++;
				}
			}
			
			if(releasedCount == this.activeEnemies.size())
			{
				this.activeEnemies.clear();
				isReleasedTexture = true;
			}
		}
	}

	
	
	
	//=================================================================================================
	//
	//	�����֐�.
	//
	//
	//=================================================================================================
	public void update()
	{
		for(int ii=0;ii<this.activeEnemies.size();ii++)
		{
			this.activeEnemies.get(ii).update();
		}

		if(inputWait > 0)
		{
			inputWait--;
			return;
		}

		if(this.skipPageRequest)
			return;
		
		if(game.isTouch && !game.event.isTouch)
		{
			//--------------------------------------
			//	���͂̕\���r���̏ꍇ�͈�C�ɕ\��������.
			//--------------------------------------
			if(MODE == BattleMode.Text ||
			   MODE == BattleMode.EnteringTurn ||
			   MODE == BattleMode.DoingTurn ||
			   MODE == BattleMode.ResultTurn)
			{
				if(!isCursorEnd)
				{
					this.skipPageRequest = true;
					//this.currentCharIndex = this.lineIndexes.get(currentLineIndex)-1;
					return;
				}
			}
			
			
			//--------------------------------------
			//	�s���҂����[�h.
			//--------------------------------------
			if(MODE == BattleMode.WaitingWhatTodo)
			{
				//�키�I��.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//�v���C���[�̍s����ނ��m�퓬�n�ɂ����Ă�.
					game.activeParty.getActivePlayer().BattleActType = BattleActType.Fight;

					//�����Ă����C�ڂ̓G��I����Ԃɂ���.
					currentTargetEnemyIndex = 0;
					while(!this.activeEnemies.get(currentTargetEnemyIndex).isAlive && currentTargetEnemyIndex < this.activeEnemies.size())
					{
						currentTargetEnemyIndex++;
					}
					this.activeEnemies.get(currentTargetEnemyIndex).startSelectedAnimation();
					
					this.askAttackTarget();
				}
				//�h��I��.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					//�v���C���[�̍s����ނ��m�h��n�ɂ����Ă�.
					game.activeParty.getActivePlayer().BattleActType = BattleActType.Guard;
					//�h�䒆�͈ꎞ�I�ɖh��͂��グ��.
					game.activeParty.getActivePlayer().guardDef = 3;

					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
				}
				//�A�C�e���I��.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					//�v���C���[�̍s����ނ��m�A�C�e���n�ɂ����Ă�.
					game.activeParty.getActivePlayer().BattleActType = BattleActType.Item;
					this.askWhatItem();
				}
				//���@
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					if(game.activeParty.getActivePlayer().magics.size() > 0)
					{
						//�v���C���[�̍s����ނ��m���@�n�ɂ����Ă�.
						game.activeParty.getActivePlayer().BattleActType = BattleActType.Magic;
						this.askWhatMagic();
					}
				}
				//����.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.3f + menuYPos && game.touchGlY < -1.05f + menuYPos)
				{
					//�v���C���[�̍s����ނ�[����]�ɐݒ�.
					game.activeParty.getActivePlayer().BattleActType = BattleActType.Run;
					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
				}
			}
			//--------------------------------------
			//	[�U��]�^�[�Q�b�g�I�����[�h.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingAttackTargetSelect)
			{
				//�J�[�\������.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					moveEnemyTarget("left");
				}
				//�J�[�\���E��.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					moveEnemyTarget("right");
				}
				
				
				//======================================================
				//	����.
				//======================================================
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//�I�������G�́y�I���ς݁z��Ԃ���������.
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					//�v���C���[�́m�^�[�Q�b�g�n��ݒ�.
					game.activeParty.getActivePlayer().Target = this.activeEnemies.get(currentTargetEnemyIndex);
					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
					//�E�F�C�g�ݒ�.
					//this.inputWait = 5;
				}
				//�߂�
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					//�I�������G�́y�I���ς݁z��Ԃ���������.
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					this.askWhatToDo();
				}
			}
			//--------------------------------------
			//	�e�L�X�g�\�����[�h.
			//--------------------------------------
			else if(MODE == BattleMode.Text)
			{
				if(this.currentLineIndex < this.lineIndexes.size()-1 && !this.skipPageRequest)
				{
					nextPageRequest = true;
				}
				else
				{
					//---------------------------------------------------------
					//	�퓬�I��.
					//---------------------------------------------------------
					if(isBattleEnded && isReleasedTexture)
					{
						//BGM�~�߂�.
						game.bgm.stop();
						
						//�l�[���v���[�g���W�߂�.
						game.activeParty.setNamePlateDungeonPosition();
						
						if(!game.event.readNextEvent())
						{
							
							String res = "";
							//--------------------------------------------------
							//	�󔠂̒��I.
							//--------------------------------------------------
							int treasureNum = 0;
							
							if(!isRunAway)
							{
								treasureNum = game.getTreasureNum();
							
								if(treasureNum > 0)
								{
									res += "image[treasure1|�����X�^�[�͕󔠂𗎂Ƃ��Ă������I]\n";
								}
	
								for(int ii=0;ii<treasureNum;ii++)
								{
									String treasure_rank = game.getTreasureRank();
									ArrayList<HashMap<String,String>> treasures = game.getTreasure(treasure_rank, Integer.toString(game.dungeon.currentPosition.floor+1));
	
									if(treasures.size() > 0)
									{
										HashMap<String, String> itemBase = treasures.get(0);
										ItemBase item = game.getItemByCode(itemBase.get("item_code"));
	
										res += "message[" + item.name + "����ɓ��ꂽ�I]\n";
										if(!game.activeParty.addItem(item, 1))
										{
											res += "message[�������̂���t���I"+item.name+"����߂�]";
										}
									}
								}
							}
							
							if(treasureNum > 0)
							{
								ArrayList<HashMap<String, String>> eventData = new ArrayList<HashMap<String,String>>();
								HashMap<String,String> eventHash = new HashMap<String, String>();
								eventHash.put("event_src", res);
								eventData.add(eventHash);
								game.event.setEvent(eventData);
							}
							else
							{
								game.event.willClose = true;
								if(isGameOver)
								{
									game.event.clearEvent();
									game.gameMode = GameMode.Title;
								}
							}
							
						}
					}
					//willClose = true;
				}
			}
			//--------------------------------------
			//	�^�[���N��.
			//--------------------------------------
			else if(MODE == BattleMode.EnteringTurn)
			{
				//�^�b�v���Ƀ^�[������.
				this.resultTurn();
			}
			//--------------------------------------
			//	�^�[������.
			//--------------------------------------
			else if(MODE == BattleMode.ResultTurn)
			{
				//�^�b�v���Ƀ^�[������.
				this.nextTurn();
			}
			//--------------------------------------
			//	�A�C�e���I��.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingItemSelect)
			{
				//�J�[�\�����.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					game.item.cursorUp();
				}
				//�J�[�\������.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					game.item.cursorDown();
				}
				//����.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//TODO.
					//�A�C�e���̎g�p.
					//�A�C�e���̍݌Ɍ��炷.
					ItemBase item = game.item.get();
					
					//
					//	�퓬���Ɏg�p�\?
					//
					if(!item.usableBattle)
					{
						game.item.updateItemDescriptionWithString("���̃A�C�e���͎g���Ȃ��I");
						return;
					}
					
					//�v���C���[��Use�v���p�e�B�ɃA�C�e����ݒ�.
					game.activeParty.getActivePlayer().Use = item;
					
					//�g�p�ΏۂɎ������g��ݒ�.
					if(item.type == ItemType.ITEM_PLAYER)
					{
						//�s���I�𒆃v���C���[���y�I����ԁz�ɂ���.
						game.activeParty.getActivePlayer().Target = game.activeParty.getActivePlayer();
						this.currentTargetPlayerIndex = game.activeParty.activePlayerIndex;
						game.activeParty.getActivePlayer().startSelectedAnimation();

					}
					else
					{
						//�ŏ��̓G���y�I����ԁz�ɂ���.
						game.activeParty.getActivePlayer().Target = this.activeEnemies.get(0);
						this.currentTargetEnemyIndex = 0;
						this.activeEnemies.get(0).startSelectedAnimation();
					}
					
					
					//�g�p�Ώۂ�I��.
					this.askUseTarget();
				}
				//�߂�
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					System.out.println("���ǂ�");
					this.askWhatToDo();
				}
			}
			//--------------------------------------
			//	[�A�C�e��]�^�[�Q�b�g�I�����[�h.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingItemTargetSelect)
			{
				ItemBase item = (ItemBase) game.activeParty.getActivePlayer().Use;
				
				//------------------------------------------------------------------------------
				//	�G�p�̓���.
				//------------------------------------------------------------------------------
				if(item.type == ItemType.ITEM_ENEMY)
				{
					//�J�[�\������.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
					{
						moveEnemyTarget("left");
					}
					//�J�[�\���E��.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						moveEnemyTarget("right");
					}
				}
				//------------------------------------------------------------------------------
				//	�v���C���[�p�̓���.
				//------------------------------------------------------------------------------
				else if(item.type == ItemType.ITEM_PLAYER)
				{
					//�J�[�\������.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
					{
						movePlayerTarget("left");
					}
					//�J�[�\���E��.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f  + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						movePlayerTarget("right");
					}
				}
				
				//======================================================
				//	����.
				//======================================================
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//�I�������G,�v���C���[�́y�I���ς݁z��Ԃ���������.
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
					
					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
					//�E�F�C�g�ݒ�.
					//this.inputWait = 5;
				}
				//�߂�
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					System.out.println("���ǂ�");
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
					MODE = BattleMode.WaitingItemSelect;
				}
			}
			//--------------------------------------
			//	���@�I��.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingMagicSelect)
			{
				//�J�[�\�����.
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
				{
					game.magic.cursorUp();
				}
				//�J�[�\������.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
				{
					game.magic.cursorDown();
				}
				//����.
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//TODO.
					//���@�̎g�p.
					MagicBase magic = game.magic.get();
					
					
					//
					//	MP����Ă�?
					//
					if(magic.mp > game.activeParty.getActivePlayer().mp)
					{
						game.magic.updateMagicDescriptionWithString("MP������Ȃ��I");
						return;
					}
					
					//
					//	�퓬���Ɏg�p�\?
					//
					if(!magic.usableBattle)
					{
						game.magic.updateMagicDescriptionWithString("���̖��@�͎g���Ȃ��I");
						return;
					}
					
					//�v���C���[��Use�v���p�e�B�ɃA�C�e����ݒ�.
					game.activeParty.getActivePlayer().Use = magic;
					//�g�p�ΏۂɎ������g��ݒ�.
					if(magic.type == MagicType.MAGIC_TYPE_CURE)
					{
						//�s���I�𒆃v���C���[���y�I����ԁz�ɂ���.
						game.activeParty.getActivePlayer().Target = game.activeParty.getActivePlayer();
						this.currentTargetPlayerIndex = game.activeParty.activePlayerIndex;
						game.activeParty.getActivePlayer().startSelectedAnimation();
					}
					else if(magic.type == MagicType.MAGIC_TYPE_ATTACK)
					{
						//�ŏ��̓G���y�I����ԁz�ɂ���.
						game.activeParty.getActivePlayer().Target = this.activeEnemies.get(0);
						this.currentTargetEnemyIndex = 0;
						this.activeEnemies.get(0).startSelectedAnimation();
					}
					
					//�g�p�Ώۂ�I��.
					this.askMagicTarget();
				}
				//�߂�
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					System.out.println("���ǂ�");
					this.askWhatToDo();
				}
			}
			//--------------------------------------
			//	[���@]�^�[�Q�b�g�I�����[�h.
			//--------------------------------------
			else if(MODE == BattleMode.WaitingMagicTargetSelect)
			{
				MagicBase magic = (MagicBase) game.activeParty.getActivePlayer().Use;
				
				//------------------------------------------------------------------------------
				//	�U�����@.
				//------------------------------------------------------------------------------
				if(magic.type == MagicType.MAGIC_TYPE_ATTACK)
				{
					//�J�[�\������.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
					{
						moveEnemyTarget("left");
					}
					//�J�[�\���E��.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						moveEnemyTarget("right");
					}
				}
				//------------------------------------------------------------------------------
				//	�񕜖��@.
				//------------------------------------------------------------------------------
				else if(magic.type == MagicType.MAGIC_TYPE_CURE)
				{
					//�J�[�\������.
					if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.65f + menuYPos && game.touchGlY < -0.45f + menuYPos)
					{
						movePlayerTarget("left");
					}
					//�J�[�\���E��.
					else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.35f + menuYPos && game.touchGlY < -0.15f + menuYPos)
					{
						movePlayerTarget("right");
					}
				}
				
				//======================================================
				//	����.
				//======================================================
				if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -0.05f + menuYPos && game.touchGlY < 0.15f + menuYPos)
				{
					//MP���炷.
					//game.activeParty.getActivePlayer().mp -= magic.mp;
					
					//�I�������G,�v���C���[�́y�I���ς݁z��Ԃ���������.
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
					
					game.activeParty.activePlayerIndex++;
					this.askWhatToDo();
					//�E�F�C�g�ݒ�.
					//this.inputWait = 5;
				}
				//�߂�
				else if(game.touchGlX > 0.99f && game.touchGlX < 1.39f && game.touchGlY > -1.0f + menuYPos && game.touchGlY < -0.75f + menuYPos)
				{
					System.out.println("���ǂ�");
					this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
					game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
					MODE = BattleMode.WaitingMagicSelect;
				}
			}
		}
	}
	
	private void moveEnemyTarget(String direct)
	{
		if(direct.equals("left"))
		{
			//�I�������G���y�I���ς݁z��Ԃɂ���.
			this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
			
			currentTargetEnemyIndex--;
			if(currentTargetEnemyIndex < 0)
				currentTargetEnemyIndex = this.activeEnemies.size()-1;
			
			this.activeEnemies.get(currentTargetEnemyIndex).startSelectedAnimation();
			
			game.activeParty.getActivePlayer().Target = this.activeEnemies.get(currentTargetEnemyIndex); 
		}
		else
		{
			//�I�������G���y�I���ς݁z��Ԃɂ���.
			this.activeEnemies.get(currentTargetEnemyIndex).stopSelectedAnimation();
			
			currentTargetEnemyIndex++;
			if(currentTargetEnemyIndex > this.activeEnemies.size()-1)
				currentTargetEnemyIndex = 0;
			
			this.activeEnemies.get(currentTargetEnemyIndex).startSelectedAnimation();
			game.activeParty.getActivePlayer().Target = this.activeEnemies.get(currentTargetEnemyIndex);
		}
	}
	
	private void movePlayerTarget(String direct)
	{
		if(direct.equals("left"))
		{
			//�I�������v���C���[���y�I���ς݁z��Ԃɂ���.
			game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();

			currentTargetPlayerIndex--;
			if(currentTargetPlayerIndex < 0)
				currentTargetPlayerIndex = game.activeParty.members.size()-1;

			Player pl = game.activeParty.members.get(currentTargetPlayerIndex);
			pl.startSelectedAnimation();
			game.activeParty.getActivePlayer().Target = pl;
		}
		else
		{
			//�I�������v���C���[���y�I���ς݁z��Ԃɂ���.
			game.activeParty.members.get(currentTargetPlayerIndex).stopSelectedAnimation();
			
			currentTargetPlayerIndex++;
			if(currentTargetPlayerIndex >  game.activeParty.members.size()-1)
				currentTargetPlayerIndex = 0;
			
			Player pl = game.activeParty.members.get(currentTargetPlayerIndex);
			pl.startSelectedAnimation();
			game.activeParty.getActivePlayer().Target = pl;
			
		}
	}
	
	private String makeEndBattleString(int exp)
	{
		String res = "";
		
		for(int ii=0;ii<game.activeParty.members.size();ii++)
		{
			Player pl = game.activeParty.members.get(ii);
			
			if(pl.isAlive && pl.isLevelUp(exp))
			{
				res += pl.name + "�̓��x���A�b�v�I\n";
			}
		}

		
		return res;
	}
}
