/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.servo.tag;

import com.netflix.servo.util.Iterables;
import com.netflix.servo.util.Preconditions;
import com.netflix.servo.util.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Immutable tag list.
 */
public final class BasicTagList implements TagList {
    /** An empty tag list. */
    public static final TagList EMPTY = new BasicTagList(Collections.<Tag>emptySet());

    private final SmallTagMap tagMap;
    private SortedMap<String, String> sortedTaglist;

    /**
     * Create a BasicTagList from a {@link SmallTagMap}.
     */
    public BasicTagList(SmallTagMap tagMap) {
        this.tagMap = tagMap;
    }

    /**
     * Creates a new instance with a fixed set of tags.
     *
     * @param entries  entries to include in this tag list
     */
    public BasicTagList(Iterable<Tag> entries) {
        SmallTagMap.Builder builder = SmallTagMap.builder();
        builder.addAll(entries);
        tagMap = builder.result();
    }

    /** {@inheritDoc} */
    public Tag getTag(String key) {
        return tagMap.get(key);
    }

    /** {@inheritDoc} */
    public String getValue(String key) {
        final Tag t = tagMap.get(key);
        return (t == null) ? null : t.getValue();
    }

    /** {@inheritDoc} */
    public boolean containsKey(String key) {
        return tagMap.containsKey(key);
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return tagMap.isEmpty();
    }

    /** {@inheritDoc} */
    public int size() {
        return tagMap.size();
    }

    /** {@inheritDoc} */
    public Iterator<Tag> iterator() {
        return tagMap.iterator();
    }

    /** {@inheritDoc} */
    public Map<String, String> asMap() {
        if (sortedTaglist != null) {
            return sortedTaglist;
        }

        SortedMap<String, String> tagMap = new TreeMap<String, String>();
        for (Tag tag : this.tagMap) {
            tagMap.put(tag.getKey(), tag.getValue());
        }
        sortedTaglist = Collections.unmodifiableSortedMap(tagMap);
        return sortedTaglist;
    }

    /**
     * Returns a new tag list with additional tags from {@code tags}. If there
     * is a conflict with tag keys the tag from {@code tags} will be used.
     */
    public BasicTagList copy(TagList tags) {
        return concat(this, tags);
    }

    /**
     * Returns a new tag list with an additional tag. If {@code key} is
     * already present in this tag list the value will be overwritten with
     * {@code value}.
     */
    public BasicTagList copy(String key, String value) {
        return concat(this, Tags.newTag(key, value));
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return this == obj
                || (obj instanceof BasicTagList) && tagMap.equals(((BasicTagList) obj).tagMap);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return tagMap.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Strings.join(",", tagMap.iterator());
    }

    /**
     * Returns a tag list containing the union of {@code t1} and {@code t2}.
     * If there is a conflict with tag keys, the tag from {@code t2} will be
     * used.
     */
    public static BasicTagList concat(TagList t1, TagList t2) {
        return new BasicTagList(Iterables.concat(t1, t2));
    }

    /**
     * Returns a tag list containing the union of {@code t1} and {@code t2}.
     * If there is a conflict with tag keys, the tag from {@code t2} will be
     * used.
     */
    public static BasicTagList concat(TagList t1, Tag... t2) {
        return new BasicTagList(Iterables.concat(t1, Arrays.asList(t2)));
    }

    /**
     * Returns a tag list from the list of key values passed.
     *
     * Example:
     *
     * <code>
     *     BasicTagList tagList = BasicTagList.of("id", "someId", "class", "someClass");
     * </code>
     */
    public static BasicTagList of(String... tags) {
        Preconditions.checkArgument(tags.length % 2 == 0,
                "tags must be a sequence of key,value pairs");

        final SmallTagMap.Builder builder = SmallTagMap.builder();
        for (int i = 0; i < tags.length; i += 2) {
            Tag t = Tags.newTag(tags[i], tags[i + 1]);
            builder.add(t);
        }
        return new BasicTagList(builder.result());
    }

    /**
     * Returns a tag list from the tags.
     */
    public static BasicTagList of(Tag... tags) {
        return new BasicTagList(Arrays.asList(tags));
    }

    /**
     * Returns a tag list that has a copy of {@code tags}.
     *
     * @deprecated Use {@link #of(Tag...)}
     */
    @Deprecated
    public static BasicTagList copyOf(Tag... tags) {
        return new BasicTagList(Arrays.asList(tags));
    }

    /**
     * Returns a tag list that has a copy of {@code tags}. Each tag value
     * is expected to be a string parseable using {@link BasicTag#parseTag}.
     *
     * @deprecated Use {@link #of(String...)} with separate key, values instead.
     */
    @Deprecated
    public static BasicTagList copyOf(String... tags) {
        return copyOf(Arrays.asList(tags));
    }

    /**
     * Returns a tag list that has a copy of {@code tags}. Each tag value
     * is expected to be a string parseable using {@link BasicTag#parseTag}.
     */
    public static BasicTagList copyOf(Iterable<String> tags) {
        SmallTagMap.Builder builder = SmallTagMap.builder();
        for (String tag : tags) {
            builder.add(Tags.parseTag(tag));
        }
        return new BasicTagList(builder.result());
    }

    /**
     * Returns a tag list that has a copy of {@code tags}.
     */
    public static BasicTagList copyOf(Map<String, String> tags) {
        SmallTagMap.Builder builder = SmallTagMap.builder();
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            builder.add(Tags.newTag(tag.getKey(), tag.getValue()));
        }
        return new BasicTagList(builder.result());
    }
}
