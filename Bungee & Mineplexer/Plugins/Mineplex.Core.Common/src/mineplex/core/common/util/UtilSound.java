package mineplex.core.common.util;

import org.bukkit.Note;

public class UtilSound 
{
	public static float GetPitch(Note note) 
	{
		int o = note.getOctave();
		switch (note.getTone()) {
		case F:
			if (note.isSharped()) {
				switch (o) { 
				case 0: return 0.5f;
				case 1: return 1f;
				case 2: return 2f;
				}
			} else {
				switch (o) {
				case 0: return 0.943874f;
				case 1: return 1.887749f;
				}
			}
			break;
		case G:
			if (note.isSharped()) {
				switch (o) { 
				case 0: return 0.561231f;
				case 1: return 1.122462f;
				}
			} else {
				switch (o) {
				case 0: return 0.529732f;
				case 1: return 1.059463f;
				}
			}
			break;
		case A:
			if (note.isSharped()) {
				switch (o) { 
				case 0: return 0.629961f;
				case 1: return 1.259921f;
				}
			} else {
				switch (o) {
				case 0: return 0.594604f;
				case 1: return 1.189207f;
				}
			}
			break;
		case B:
			switch (o) {
			case 0: return 0.667420f;
			case 1: return 1.334840f;
			}
			break;
		case C:
			if (note.isSharped()) {
				switch (o) { 
				case 0: return 0.749154f;
				case 1: return 1.498307f;
				}
			} else {
				switch (o) {
				case 0: return 0.707107f;
				case 1: return 1.414214f;
				}
			}
			break;

		case D:
			if (note.isSharped()) {
				switch (o) { 
				case 0: return 0.840896f;
				case 1: return 1.681793f;
				}
			} else {
				switch (o) {
				case 0: return 0.793701f;
				case 1: return 1.587401f;
				}
			}
			break;
		case E:
			switch (o) {
			case 0: return 0.890899f;
			case 1: return 1.781797f;
			}
		}
		return -1f;
	}
}
