/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.java.sk89q.jnbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.util.Vector;

import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagByteArray;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagEnd;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagIntArray;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagShort;
import net.minecraft.server.v1_8_R3.NBTTagString;

/**
 * A class which contains NBT-related utility methods.
 *
 */
public final class NBTUtils {

    /**
     * Default private constructor.
     */
    private NBTUtils() {
    }

    /**
     * Gets the type name of a tag.
     *
     * @param clazz the tag class
     * @return The type name.
     */
    public static String getTypeName(Class<? extends Tag> clazz) {
        if (clazz.equals(ByteArrayTag.class)) {
            return "TAG_Byte_Array";
        } else if (clazz.equals(ByteTag.class)) {
            return "TAG_Byte";
        } else if (clazz.equals(CompoundTag.class)) {
            return "TAG_Compound";
        } else if (clazz.equals(DoubleTag.class)) {
            return "TAG_Double";
        } else if (clazz.equals(EndTag.class)) {
            return "TAG_End";
        } else if (clazz.equals(FloatTag.class)) {
            return "TAG_Float";
        } else if (clazz.equals(IntTag.class)) {
            return "TAG_Int";
        } else if (clazz.equals(ListTag.class)) {
            return "TAG_List";
        } else if (clazz.equals(LongTag.class)) {
            return "TAG_Long";
        } else if (clazz.equals(ShortTag.class)) {
            return "TAG_Short";
        } else if (clazz.equals(StringTag.class)) {
            return "TAG_String";
        } else if (clazz.equals(IntArrayTag.class)) {
            return "TAG_Int_Array";
        } else {
            throw new IllegalArgumentException("Invalid tag classs ("
                    + clazz.getName() + ").");
        }
    }

    /**
     * Gets the type code of a tag class.
     *
     * @param clazz the tag class
     * @return The type code.
     * @throws IllegalArgumentException if the tag class is invalid.
     */
    public static int getTypeCode(Class<? extends Tag> clazz) {
        if (clazz.equals(ByteArrayTag.class)) {
            return NBTConstants.TYPE_BYTE_ARRAY;
        } else if (clazz.equals(ByteTag.class)) {
            return NBTConstants.TYPE_BYTE;
        } else if (clazz.equals(CompoundTag.class)) {
            return NBTConstants.TYPE_COMPOUND;
        } else if (clazz.equals(DoubleTag.class)) {
            return NBTConstants.TYPE_DOUBLE;
        } else if (clazz.equals(EndTag.class)) {
            return NBTConstants.TYPE_END;
        } else if (clazz.equals(FloatTag.class)) {
            return NBTConstants.TYPE_FLOAT;
        } else if (clazz.equals(IntTag.class)) {
            return NBTConstants.TYPE_INT;
        } else if (clazz.equals(ListTag.class)) {
            return NBTConstants.TYPE_LIST;
        } else if (clazz.equals(LongTag.class)) {
            return NBTConstants.TYPE_LONG;
        } else if (clazz.equals(ShortTag.class)) {
            return NBTConstants.TYPE_SHORT;
        } else if (clazz.equals(StringTag.class)) {
            return NBTConstants.TYPE_STRING;
        } else if (clazz.equals(IntArrayTag.class)) {
            return NBTConstants.TYPE_INT_ARRAY;
        } else {
            throw new IllegalArgumentException("Invalid tag classs ("
                    + clazz.getName() + ").");
        }
    }

    /**
     * Gets the class of a type of tag.
     *
     * @param type the type
     * @return The class.
     * @throws IllegalArgumentException if the tag type is invalid.
     */
    public static Class<? extends Tag> getTypeClass(int type) {
        switch (type) {
        case NBTConstants.TYPE_END:
            return EndTag.class;
        case NBTConstants.TYPE_BYTE:
            return ByteTag.class;
        case NBTConstants.TYPE_SHORT:
            return ShortTag.class;
        case NBTConstants.TYPE_INT:
            return IntTag.class;
        case NBTConstants.TYPE_LONG:
            return LongTag.class;
        case NBTConstants.TYPE_FLOAT:
            return FloatTag.class;
        case NBTConstants.TYPE_DOUBLE:
            return DoubleTag.class;
        case NBTConstants.TYPE_BYTE_ARRAY:
            return ByteArrayTag.class;
        case NBTConstants.TYPE_STRING:
            return StringTag.class;
        case NBTConstants.TYPE_LIST:
            return ListTag.class;
        case NBTConstants.TYPE_COMPOUND:
            return CompoundTag.class;
        case NBTConstants.TYPE_INT_ARRAY:
            return IntArrayTag.class;
        default:
            throw new IllegalArgumentException("Invalid tag type : " + type
                    + ".");
        }
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items the map to read from
     * @param key the key to look for
     * @param expected the expected NBT class type
     * @return child tag
     * @throws IllegalArgumentException
     */
    public static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws IllegalArgumentException {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
    
    
    public static byte[] toBytesCompressed(String name, CompoundTag tag)
    {
    	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    	try (NBTOutputStream nbtStream = new NBTOutputStream(new GZIPOutputStream(byteStream)))
    	{
    		nbtStream.writeNamedTag(name, tag);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    	return byteStream.toByteArray();
    }
    
    public static NamedTag getFromBytesCompressed(byte[] bytes)
    {
    	try (NBTInputStream stream = new NBTInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));)
    	{
    		return stream.readNamedTag();
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    }
    
    
    public static NBTBase toNative(Tag tag) {
        if (tag instanceof IntArrayTag) {
            return toNative((IntArrayTag) tag);

        } else if (tag instanceof ListTag) {
            return toNative((ListTag) tag);

        } else if (tag instanceof LongTag) {
            return toNative((LongTag) tag);

        } else if (tag instanceof StringTag) {
            return toNative((StringTag) tag);

        } else if (tag instanceof IntTag) {
            return toNative((IntTag) tag);

        } else if (tag instanceof ByteTag) {
            return toNative((ByteTag) tag);

        } else if (tag instanceof ByteArrayTag) {
            return toNative((ByteArrayTag) tag);

        } else if (tag instanceof CompoundTag) {
            return toNative((CompoundTag) tag);

        } else if (tag instanceof FloatTag) {
            return toNative((FloatTag) tag);

        } else if (tag instanceof ShortTag) {
            return toNative((ShortTag) tag);

        } else if (tag instanceof DoubleTag) {
            return toNative((DoubleTag) tag);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static NBTTagIntArray toNative(IntArrayTag tag) {
        int[] value = tag.getValue();
        return new NBTTagIntArray(Arrays.copyOf(value, value.length));
    }

    public static NBTTagList toNative(ListTag tag) {
        NBTTagList list = new NBTTagList();
        for (Tag child : tag.getValue()) {
            if (child instanceof EndTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static NBTTagLong toNative(LongTag tag) {
        return new NBTTagLong(tag.getValue());
    }

    public static NBTTagString toNative(StringTag tag) {
        return new NBTTagString(tag.getValue());
    }

    public static NBTTagInt toNative(IntTag tag) {
        return new NBTTagInt(tag.getValue());
    }

    public static NBTTagByte toNative(ByteTag tag) {
        return new NBTTagByte(tag.getValue());
    }

    public static NBTTagByteArray toNative(ByteArrayTag tag) {
        byte[] value = tag.getValue();
        return new NBTTagByteArray(Arrays.copyOf(value, value.length));
    }

    public static NBTTagCompound toNative(CompoundTag tag) {
        NBTTagCompound compound = new NBTTagCompound();
        for (Entry<String, Tag> child : tag.getValue().entrySet()) {
            compound.set(child.getKey(), toNative(child.getValue()));
        }
        return compound;
    }

    public static NBTTagFloat toNative(FloatTag tag) {
        return new NBTTagFloat(tag.getValue());
    }

    public static NBTTagShort toNative(ShortTag tag) {
        return new NBTTagShort(tag.getValue());
    }

    public static NBTTagDouble toNative(DoubleTag tag) {
        return new NBTTagDouble(tag.getValue());
    }

    public static Tag fromNative(NBTBase other) {
        if (other instanceof NBTTagIntArray) {
            return fromNative((NBTTagIntArray) other);

        } else if (other instanceof NBTTagList) {
            return fromNative((NBTTagList) other);

        } else if (other instanceof NBTTagEnd) {
            return fromNative((NBTTagEnd) other);

        } else if (other instanceof NBTTagLong) {
            return fromNative((NBTTagLong) other);

        } else if (other instanceof NBTTagString) {
            return fromNative((NBTTagString) other);

        } else if (other instanceof NBTTagInt) {
            return fromNative((NBTTagInt) other);

        } else if (other instanceof NBTTagByte) {
            return fromNative((NBTTagByte) other);

        } else if (other instanceof NBTTagByteArray) {
            return fromNative((NBTTagByteArray) other);

        } else if (other instanceof NBTTagCompound) {
            return fromNative((NBTTagCompound) other);

        } else if (other instanceof NBTTagFloat) {
            return fromNative((NBTTagFloat) other);

        } else if (other instanceof NBTTagShort) {
            return fromNative((NBTTagShort) other);

        } else if (other instanceof NBTTagDouble) {
            return fromNative((NBTTagDouble) other);
        } else {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
    }

    public static IntArrayTag fromNative(NBTTagIntArray other) {
        int[] value = other.c();
        return new IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static ListTag fromNative(NBTTagList other) {
        other = (NBTTagList) other.clone();
        List<Tag> list = new ArrayList<Tag>();
        Class<? extends Tag> listClass = StringTag.class;
        int size = other.size();
        for (int i = 0; i < size; i++) {
            Tag child = fromNative(other.a(0));
            list.add(child);
            listClass = child.getClass();
        }
        return new ListTag(listClass, list);
    }

    public static EndTag fromNative(NBTTagEnd other) {
        return new EndTag();
    }

    public static LongTag fromNative(NBTTagLong other) {
        return new LongTag(other.c());
    }

    public static StringTag fromNative(NBTTagString other) {
        return new StringTag(other.a_());
    }

    public static IntTag fromNative(NBTTagInt other) {
        return new IntTag(other.d());
    }

    public static ByteTag fromNative(NBTTagByte other) {
        return new ByteTag(other.f());
    }

    public static ByteArrayTag fromNative(NBTTagByteArray other) {
        byte[] value = other.c();
        return new ByteArrayTag(Arrays.copyOf(value, value.length));
    }

    public static CompoundTag fromNative(NBTTagCompound other) {
        @SuppressWarnings("unchecked") Collection<String> tags = other.c();
        Map<String, Tag> map = new HashMap<String, Tag>();
        for (String tagName : tags) {
            map.put(tagName, fromNative(other.get(tagName)));
        }
        return new CompoundTag(map);
    }

    public static FloatTag fromNative(NBTTagFloat other) {
        return new FloatTag(other.h());
    }

    public static ShortTag fromNative(NBTTagShort other) {
        return new ShortTag(other.e());
    }

    public static DoubleTag fromNative(NBTTagDouble other) {
        return new DoubleTag(other.g());
    }
    
    public static NBTTagList doubleArrayToList(double... doubles)
    {
      NBTTagList nbttaglist = new NBTTagList();
      for(double d : doubles)
      {
    	  nbttaglist.add(new NBTTagDouble(d));
      }
      return nbttaglist;
    }
    
    public static Vector getVector(CompoundTag tag)
    {
    	return new Vector(tag.asDouble("x"), tag.asDouble("y"), tag.asDouble("z"));
    }

}
