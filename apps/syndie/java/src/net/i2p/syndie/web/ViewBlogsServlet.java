package net.i2p.syndie.web;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.i2p.I2PAppContext;
import net.i2p.client.naming.*;
import net.i2p.data.*;
import net.i2p.syndie.*;
import net.i2p.syndie.data.*;
import net.i2p.syndie.sml.*;

/**
 * List the blogs known in the archive
 *
 */
public class ViewBlogsServlet extends BaseServlet {
    private static final int MAX_AUTHORS_AT_ONCE = 20;
    private static final int MAX_TAGS = 50;
    
    /** renders the posts from the last 3 days */
    private String getViewBlogLink(Hash blog, long lastPost) {
        long dayBegin = BlogManager.instance().getDayBegin();
        int daysAgo = 2;
        if ( (lastPost > 0) && (dayBegin - 3*24*60*6081000 > lastPost) ) // last post was old 3 days ago
            daysAgo = (int)((dayBegin - lastPost + 24*60*60*1000-1)/(24*60*60*1000));
        daysAgo++;
        return getControlTarget() + "?" + ThreadedHTMLRenderer.PARAM_AUTHOR + '=' + blog.toBase64()
               + '&' + ThreadedHTMLRenderer.PARAM_THREAD_AUTHOR + "=true&daysBack=" + daysAgo;
    }
    
    private String getPostDate(long when) {
        String age = null;
        long dayBegin = BlogManager.instance().getDayBegin();
        long postId = when;
        if (postId >= dayBegin) {
            age = "today";
        } else if (postId >= dayBegin - 24*60*60*1000) {
            age = "yesterday";
        } else {
            int daysAgo = (int)((dayBegin - postId + 24*60*60*1000-1)/(24*60*60*1000));
            age = daysAgo + " days ago";
        }
        return age;
    }
    
    protected void renderServletDetails(User user, HttpServletRequest req, PrintWriter out, ThreadIndex index, 
                                        int threadOffset, BlogURI visibleEntry, Archive archive) throws IOException {
        TreeSet orderedRoots = new TreeSet(new NewestEntryFirstComparator());
        // The thread index is ordered by last updated date, as opposed to root posting date,
        // so lets reorder things
        int count = index.getRootCount();
        for (int i = 0; i < count; i++) {
            ThreadNode node = index.getRoot(i);
            orderedRoots.add(node.getEntry());
        }
        
        TreeSet tags = new TreeSet();
        List writtenAuthors = new ArrayList();
        
        
        out.write("<tr><td colspan=\"3\" valign=\"top\" align=\"left\"><span class=\"syndieBlogFavorites\">");
        if ( (user != null) && (user.getAuthenticated()) ) {
            out.write("<b>Favorite blogs:</b><br />\n");
            out.write("<a href=\"" + getViewBlogLink(user.getBlog(), user.getLastMetaEntry()) 
                      + "\" title=\"View your blog\">Your blog</a><br />\n");
            
            PetNameDB db = user.getPetNameDB();
            for (Iterator iter = orderedRoots.iterator(); iter.hasNext() && writtenAuthors.size() < MAX_AUTHORS_AT_ONCE; ) {
                BlogURI uri= (BlogURI)iter.next();
                if (writtenAuthors.contains(uri.getKeyHash())) {
                    // skip
                } else {
                    PetName pn = db.getByLocation(uri.getKeyHash().toBase64());
                    if (pn != null) {
                        if (pn.isMember(FilteredThreadIndex.GROUP_FAVORITE)) {
                            out.write("<a href=\"" + getViewBlogLink(uri.getKeyHash(), uri.getEntryId()) 
                                      + "\" title=\"View " + HTMLRenderer.sanitizeTagParam(pn.getName()) +"'s blog\">");
                            out.write(HTMLRenderer.sanitizeString(pn.getName(), 32));
                            out.write("</a> (" + getPostDate(uri.getEntryId()) + ")<br />\n");
                            writtenAuthors.add(uri.getKeyHash());
                        } else if (pn.isMember(FilteredThreadIndex.GROUP_IGNORE)) {
                            // ignore 'em
                            writtenAuthors.add(uri.getKeyHash());
                        } else {
                            // bookmarked, but not a favorite... leave them for later
                        }
                    } else {
                        // not bookmarked, leave them for later
                    }
                }
            }
        }
        out.write("</span>\n");
    
        // now for the non-bookmarked people
        out.write("<span class=\"syndieBlogList\">");
        out.write("<b>Most recently updated blogs:</b><br />\n");
        for (Iterator iter = orderedRoots.iterator(); iter.hasNext() && writtenAuthors.size() < MAX_AUTHORS_AT_ONCE; ) {
            BlogURI uri= (BlogURI)iter.next();
            String curTags[] = archive.getEntry(uri).getTags();
            if (curTags != null)
                for (int i = 0; i < curTags.length && tags.size() < MAX_TAGS; i++)
                    tags.add(curTags[i]);
            if (writtenAuthors.contains(uri.getKeyHash())) {
                // skip
            } else {
                BlogInfo info = archive.getBlogInfo(uri);
                if (info == null)
                    continue;
                String name = info.getProperty(BlogInfo.NAME);
                if ( (name == null) || (name.trim().length() <= 0) )
                    name = uri.getKeyHash().toBase64().substring(0,8);
                String desc = info.getProperty(BlogInfo.DESCRIPTION);
                if ( (desc == null) || (desc.trim().length() <= 0) ) 
                    desc = name + "'s blog";
                String age = null;
                long dayBegin = BlogManager.instance().getDayBegin();
                long postId = uri.getEntryId();
                if (postId >= dayBegin) {
                    age = "today";
                } else if (postId >= dayBegin - 24*60*60*1000) {
                    age = "yesterday";
                } else {
                    int daysAgo = (int)((dayBegin - postId + 24*60*60*1000-1)/(24*60*60*1000));
                    age = daysAgo + " days ago";
                }
                
                out.write("<a href=\"" + getViewBlogLink(uri.getKeyHash(), uri.getEntryId())
                          + "\" title=\"View " + trim(HTMLRenderer.sanitizeTagParam(name), 32)
                          + "'s blog\">");
                out.write(HTMLRenderer.sanitizeString(desc, 32));
                out.write("</a> (" + getPostDate(uri.getEntryId()) + ")<br />\n");
                writtenAuthors.add(uri.getKeyHash());
            }
        }
        
        out.write("</span>\n");
        /*
        out.write("<tr><td colspan=\"3\"><b>Topics:</b></td></tr>\n");
        out.write("<tr><td colspan=\"3\">");
        for (Iterator iter = tags.iterator(); iter.hasNext(); ) {
            String tag = (String)iter.next();
            out.write("<a href=\"" + ThreadedHTMLRenderer.getFilterByTagLink(getControlTarget(), null, user, tag, null) 
                      + "\" title=\"View threads flagged with the tag '" + HTMLRenderer.sanitizeTagParam(tag) + "'\">");
            out.write(HTMLRenderer.sanitizeString(tag, 32));
            out.write("</a> ");
        }
         */
        out.write("</td></tr>\n");
    }
    
    protected String getTitle() { return "Syndie :: View blogs"; }
}
