/*
 * Whiteness. Copyright (c) 2007-2010, Alexander Tuganov.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package ru.tuganoff.whiteness.core;

import ru.tuganoff.whiteness.application.ApplicationFactory;
import ru.tuganoff.whiteness.component.Form;
import ru.tuganoff.whiteness.component.PopupMenu;
import ru.tuganoff.whiteness.component.annotation.ResultSafe;
import ru.tuganoff.whiteness.component.core.*;
import ru.tuganoff.whiteness.component.core.annotations.BrowserEvent;
import ru.tuganoff.whiteness.component.core.supports.internal.*;
import ru.tuganoff.whiteness.component.decorate.*;
import ru.tuganoff.whiteness.component.decorate.core.ActualFont;
import ru.tuganoff.whiteness.component.decorate.core.LayoutIndents;
import ru.tuganoff.whiteness.component.event.core.Event;
import ru.tuganoff.whiteness.component.event.core.EventDelegate;
import ru.tuganoff.whiteness.component.feature.Activable;
import ru.tuganoff.whiteness.component.feature.Focusable;
import ru.tuganoff.whiteness.component.feature.Localable;
import ru.tuganoff.whiteness.component.feature.Named;
import ru.tuganoff.whiteness.component.MainMenu;
import ru.tuganoff.whiteness.component.util.EventBinder;
import ru.tuganoff.whiteness.component.util.FieldBinder;
import ru.tuganoff.whiteness.component.util.Loader;
import ru.tuganoff.whiteness.component.util.loader.IgnoreProperty;
import ru.tuganoff.whiteness.context.RenderContext;
import ru.tuganoff.whiteness.exception.ResourceNotFoundException;
import ru.tuganoff.whiteness.exception.WhitenessCloseException;
import ru.tuganoff.whiteness.exception.WhitenessException;
import ru.tuganoff.whiteness.exception.core.WhitenessInternalError;
import ru.tuganoff.whiteness.instance.ClientInformation;
import ru.tuganoff.whiteness.instance.Executable;
import ru.tuganoff.whiteness.instance.Instance;
import ru.tuganoff.whiteness.instance.InstanceImpl;
import ru.tuganoff.whiteness.render.WhitenessRender;
import ru.tuganoff.whiteness.util.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: Александр Туганов
 * Date: 26.01.2008
 * Time: 20:44:45
 */

// fix_TODO: Whiteness.js и Whiteness -- сделать класс для whiteness -- "whiteness theme_name".
// fix_TODO: Убрать вертикальную полосу прокрутки.
// fix_TODO: Перенети настройку фонта и размера кнопок заголовка в Whiteness.
// fix_TODO: Оповещать всё формы об изменении фонта и размера кнопок.
// TODO: При появлении или скрытия полос прокрутки clientRect изменяется.

// fix_TODO: При щелчке на Whiteness не брать на себя фокус, подобно форме.    
// fix_TODO: Стрелки Left, Top, Right, Bottom для управления фокусом.    

// fix_TODO: Убрать ApplicationType

public final class Whiteness extends AbstractElement implements Container, Localable {
//    private final static Logger log = Logger.getLogger(Whiteness.class);

    public interface ResizeEvent extends Event {
        public void onResize(Whiteness whiteness) throws WhitenessException, WhitenessCloseException;
    }

//    public interface ActivateEvent extends Event {
//        public boolean onActivate(Component component) throws WhitenessException, WhitenessCloseException;
//    }

//    public interface DeactivateEvent extends Event {
//        public boolean onDeactivate(Component component) throws WhitenessException, WhitenessCloseException;
//    }

    public interface FocusedChangingEvent extends Event {
        public void onFocusedChanging(Whiteness whiteness, Focusable next);
    }

    public interface FocusedChangedEvent extends Event {
        public void onFocusedChanged(Whiteness whiteness);
    }

    public interface EnterEvent extends Event {
        public void onEnter(Whiteness whiteness, Focusable previous);
    }

    public interface ExitEvent extends Event {
        public void onExit(Whiteness whiteness, Focusable next);
    }

    public interface KeyDownEvent extends Event {
        public boolean onKeyDown(Whiteness whiteness, Key key, boolean repeat) throws WhitenessException, WhitenessCloseException;
    }

    public interface KeyUpEvent extends Event {
        public boolean onKeyUp(Whiteness whiteness, Key key) throws WhitenessException, WhitenessCloseException;
    }

    public interface KeyPressEvent extends Event {
        public boolean onKeyPress(Whiteness whiteness, char key, EnumSet<Key.Shift> modifier) throws WhitenessException, WhitenessCloseException;
    }

    public interface ClosedEvent extends Event {
        public void onClosed(Whiteness whiteness);
    }

    public final static String VERSION = "1.4.0.GA";

    private final InstanceImpl instance;

    private final Appearance appearance = new Appearance(this);
    private final ShortcutCenter shortcutCenter = new ShortcutCenter();
    private final WhitenessChildren children = new WhitenessChildren(this);
    private final Rect rect = new Rect();

    private Locale locale = null;

    private boolean selectAllowed = false;
    LayoutIndents layoutRect = null;
    private Cursor cursor = null;
    private Border border = null;
    private Background background = null;
    private Color color = null;
    private Font font = null;
    private Scroll scroll = null;
    public MainMenu menu = null;
    private PopupMenu popupMenu = null;
    
    private boolean keyboardCueUnderlinesShowed = false;
    private boolean focusRectanglesShowed = false;

    private Focusable focused = null;

    private final EventDelegate<Whiteness.ResizeEvent> onResize = new EventDelegate<Whiteness.ResizeEvent>();
    private final EventDelegate<Whiteness.ContextMenuEvent> onContextMenu = new EventDelegate<Whiteness.ContextMenuEvent>();
    private final EventDelegate<Element.PreviewKeyDownEvent> onPreviewKeyDown = new EventDelegate<Whiteness.PreviewKeyDownEvent>(new EventDelegate.Listener() {
        @Override
        public void appear() {
            getInternal().getRender().renderOnPreviewKeyDown(Whiteness.this);
        }

        @Override
        public void disappear() {
        }
    });
    private final EventDelegate<Whiteness.KeyDownEvent> onKeyDown = new EventDelegate<Whiteness.KeyDownEvent>(new EventDelegate.Listener() {
        @Override
        public void appear() {
            getInternal().getRender().renderOnKeyDown(Whiteness.this);
        }

        @Override
        public void disappear() {
        }
    });
    private final EventDelegate<Whiteness.KeyUpEvent> onKeyUp = new EventDelegate<Whiteness.KeyUpEvent>(new EventDelegate.Listener() {
        @Override
        public void appear() {
            getInternal().getRender().renderOnKeyUp(Whiteness.this);
        }

        @Override
        public void disappear() {
        }
    });
    private final EventDelegate<Whiteness.KeyPressEvent> onKeyPress = new EventDelegate<Whiteness.KeyPressEvent>(new EventDelegate.Listener() {
        @Override
        public void appear() {
            getInternal().getRender().renderOnKeyPress(Whiteness.this);
        }

        @Override
        public void disappear() {
        }
    });
    private final EventDelegate<Whiteness.FocusedChangingEvent> onFocusedChanging = new EventDelegate<Whiteness.FocusedChangingEvent>();
    private final EventDelegate<Whiteness.FocusedChangedEvent> onFocusedChanged = new EventDelegate<Whiteness.FocusedChangedEvent>();
    private final EventDelegate<Whiteness.EnterEvent> onEnter = new EventDelegate<Whiteness.EnterEvent>();
    private final EventDelegate<Whiteness.ExitEvent> onExit = new EventDelegate<Whiteness.ExitEvent>();
    private final EventDelegate<Whiteness.ClosedEvent> onClosed = new EventDelegate<Whiteness.ClosedEvent>();


    private final Map<Integer, Element> allElements = new HashMap<Integer, Element>();
    private final Map<Element, Integer> allElementIds = new HashMap<Element, Integer>();
    private final ReentrantLock ELEMENTS_LOCK = new ReentrantLock();

    private final List<AppearanceSupport> appearanceSupportList = new CopyOnWriteArrayList<AppearanceSupport>();
    private final List<LocaleSupport> localeSupportList = new CopyOnWriteArrayList<LocaleSupport>();
    private final List<KeyboardCueUnderlinesSupport> keyboardCueUnderlinesSupportList = new CopyOnWriteArrayList<KeyboardCueUnderlinesSupport>();



    {
        shortcutCenter.addShortcut(CommonShortcuts.MENU_SHOW_KEYBOARD_CUE_UNDERLINES_SHORTCUT, new ShortcutEvent() {
            @Override
            public boolean onShortcut(Key key, boolean reduced, boolean repeat) {
                if (!repeat && !getInternal().isKeyboardCueUnderlinesShowed())
                    invokeNotWait(new Executable() {
                        @Override
                        public void execute() throws WhitenessException, WhitenessCloseException {
                            getInternal().showKeyboardCueUnderlines();
                        }
                    });
                return false;
            }
        });
        shortcutCenter.suppressShortcut(CommonShortcuts.CLOSE_SHORTCUT); // TODO: Сделать, как Form.setCloseShortcut + может быть форма каким-то образом будет брать шорткут поумолчанию отсюда... (null не годится -- это говорит, что просто шортсута на закрытие нет)

        shortcutCenter.addShortcut(CommonShortcuts.TAB_SHORTCUT, new ShortcutEvent() {
            @Override
            public boolean onShortcut(Key key, boolean reduced, boolean repeat) throws WhitenessException, WhitenessCloseException {
                invokeNotWait(new Executable() {
                    @Override
                    public void execute() throws WhitenessException, WhitenessCloseException {
                        children.focusNextTabStopComponent();
                        getWhiteness().getInternal().showFocusRectangles();
                    }
                });
                return true;
            }
        });
        shortcutCenter.addShortcut(CommonShortcuts.SHIFT_TAB_SHORTCUT, new ShortcutEvent() {
            @Override
            public boolean onShortcut(Key key, boolean reduced, boolean repeat) throws WhitenessException, WhitenessCloseException {
                invokeNotWait(new Executable() {
                    @Override
                    public void execute() throws WhitenessException, WhitenessCloseException {
                        children.focusPrevTabStopComponent();
                        getWhiteness().getInternal().showFocusRectangles();
                    }
                });
                return true;
            }
        });
        shortcutCenter.addShortcuts(CommonShortcuts.NEXT_ARROW_SHORTCUTS, new ShortcutEvent() {
            @Override
            public boolean onShortcut(Key key, boolean reduced, boolean repeat) throws WhitenessException, WhitenessCloseException {
                invokeNotWait(new Executable() {
                    @Override
                    public void execute() throws WhitenessException, WhitenessCloseException {
                        children.focusNextTabStopComponentInContainer();
                        getWhiteness().getInternal().showFocusRectangles();
                    }
                });
                return true;
            }
        });
        shortcutCenter.addShortcuts(CommonShortcuts.PREV_ARROW_SHORTCUTS, new ShortcutEvent() {
            @Override
            public boolean onShortcut(Key key, boolean reduced, boolean repeat) throws WhitenessException, WhitenessCloseException {
                invokeNotWait(new Executable() {
                    @Override
                    public void execute() throws WhitenessException, WhitenessCloseException {
                        children.focusPrevTabStopComponentInContainer();
                        getWhiteness().getInternal().showFocusRectangles();
                    }
                });
                return true;
            }
        });
    }

    public Whiteness(InstanceImpl instance) {
        this.instance = instance;
        this.locale = instance.getApplication().getDefaultLocale();
//        setWhiteness(this); <-- onRefresh это делает!
        getInternal().elementAdded(this); // В super.setWhiteness() тоже componentAdded вызываеться, но делать нечего, здесь он нужен, а то onRefresh не прийдёт.
    }

    public void load(Class clazz) throws ResourceNotFoundException, IOException {
        String resource = Loader.getResource(clazz);
        InputStream in = Resources.getResourceAsStream(resource, getInstance().getServletContext());
        if (in == null)
            throw new ResourceNotFoundException(resource);
        Loader.getInstance().load(this, in);
    }

    public void load(Object object) throws ResourceNotFoundException, IOException {
        String resource = Loader.getResource(object);
        InputStream in = Resources.getResourceAsStream(resource, getInstance().getServletContext());
        if (in == null)
            throw new ResourceNotFoundException(resource);
        Loader.getInstance().load(this, in);
        if (object != null) {
            FieldBinder.bindElementsToFields(this, object);
            EventBinder.bindElementEvents(this, object);
        }
    }

    public void load(Object object, String resource) throws ResourceNotFoundException, IOException {
        InputStream in = Resources.getResourceAsStream(resource, getInstance().getServletContext());
        if (in == null)
            throw new ResourceNotFoundException(resource);
        Loader.getInstance().load(this, in);
        if (object != null) {
            FieldBinder.bindElementsToFields(this, object);
            EventBinder.bindElementEvents(this, object);
        }
    }

    @Override @IgnoreProperty
    public Instance getInstance() {
        return instance;
    }

     @IgnoreProperty
    public Appearance getAppearance() {
        return appearance;
    }

    @Override @IgnoreProperty
    public WhitenessChildren getChildren() {
        return children;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        getInternal().getRender().renderLocale(this);
//        getChildren().getInternal().localeChanged();
        localeChanged();
    }

    private void localeChanged() {
        for (LocaleSupport localeSupport : localeSupportList)
            localeSupport.localeChanged();
    }

    @IgnoreProperty
    public boolean isSelectAllowed() {
        return selectAllowed;
    }

    public void setSelectAllowed(boolean selectAllowed) {
        this.selectAllowed = selectAllowed;
        getInternal().getRender().renderSelectAllowed(this);
    }

    @Override @IgnoreProperty
    public Rect getClientRect() {
        return rect.getRect();
    }

    @Override @IgnoreProperty
    public Size getClientSize() {
        return rect.getSize();
    }

    @Override
    public void clientToWhiteness(Point point) {
    }

    @Override
    public void clientToWhiteness(Rect rect) {
    }

    @Override
    public boolean isElementInHierarchy(ParentedElement element) {
        Parent c = element != null ? element.getContainer() : null;
        while (c != null) {
            if (c == this)
                return true;
            c = c instanceof ParentedElement ? ((ParentedElement)c).getContainer() : null;
        }
        return false;
    }

    @IgnoreProperty
    public Size getSize() {
        return rect.getSize();
    }

    @IgnoreProperty
    public int getWidth() {
        return rect.getWidth();
    }

    @IgnoreProperty
    public int getHeight() {
        return rect.getHeight();
    }

    @Override @ResultSafe
    public LayoutIndents getLayoutRect() {
        return layoutRect != null ? layoutRect.getLayoutRect() : null;
    }

    public void setLayoutRect(LayoutIndents layoutRect) {
        this.layoutRect = layoutRect != null ? layoutRect.getLayoutRect() : null;
        getChildren().getInternal().doLoyaut();
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        getInternal().getRender().renderCursor(this);
    }

    public Border getBorder() {
        return border;
    }

    public void setBorder(Border border) {
        this.border = border;
        getInternal().getRender().renderBorder(this);
    }

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
        getInternal().getRender().renderBackground(this);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        getInternal().getRender().renderColor(this);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
        getInternal().setActualFont();
        getInternal().getRender().renderFont(this);
        fontChanged();
    }

    protected void fontChanged() {
    }

    public Scroll getScroll() {
        return scroll;
    }

    public void setScroll(Scroll scroll) {
        this.scroll = scroll;
        getInternal().getRender().renderScroll(this);
    }

    public MainMenu getMenu() {
        return menu;
    }

    public void setMenu(MainMenu menu) {
        this.menu = menu;
    }

    public PopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void setPopupMenu(PopupMenu popupMenu) {
        if (this.popupMenu != null)
            this.popupMenu.getInternal().removeOwner(this);
        this.popupMenu = popupMenu;
        if (this.popupMenu != null)
            this.popupMenu.getInternal().addOwner(this);
    }

    protected void closed() {
        for (Whiteness.ClosedEvent event : onClosed)
            event.onClosed(this);
    }

    public EventDelegate<ResizeEvent> onResize() {
        return onResize;
    }

    public EventDelegate<Whiteness.ContextMenuEvent> onContextMenu() {
        return onContextMenu;
    }

    public EventDelegate<Whiteness.PreviewKeyDownEvent> onPreviewKeyDown() {
        return onPreviewKeyDown;
    }

    public EventDelegate<Whiteness.KeyDownEvent> onKeyDown() {
        return onKeyDown;
    }

    public EventDelegate<Whiteness.KeyUpEvent> onKeyUp() {
        return onKeyUp;
    }

    public EventDelegate<Whiteness.KeyPressEvent> onKeyPress() {
        return onKeyPress;
    }

    public EventDelegate<Whiteness.FocusedChangingEvent> onFocusedChanging() {
        return onFocusedChanging;
    }

    public EventDelegate<Whiteness.FocusedChangedEvent> onFocusedChanged() {
        return onFocusedChanged;
    }

    public EventDelegate<Whiteness.EnterEvent> onEnter() {
        return onEnter;
    }

    public EventDelegate<Whiteness.ExitEvent> onExit() {
        return onExit;
    }

    public EventDelegate<Whiteness.ClosedEvent> onClose() {
        return onClosed;
    }

    private final ReentrantLock FOCUSED_LOCK = new ReentrantLock();

    public Focusable getFocused() {
        final ReentrantLock lock = this.FOCUSED_LOCK;
        lock.lock();
        try {
            return focused;
        } finally {
            lock.unlock();
        }
    }

    public boolean setFocused(Focusable focused) {
        return setFocused(focused, true, false);
    }

    private boolean setFocused(Focusable focused, boolean render, boolean force) {
        final ReentrantLock lock = this.FOCUSED_LOCK;
        lock.lock();
        try {
            if (this.focused == focused)
                return true;
            if (focused != null && focused.getWhiteness() != this)
                throw new IllegalArgumentException("Invalid whiteness.");

            final Focusable oldFocused = this.focused;
            if (focused != null && !focused.canFocus())
                if (!force)
                    return false;
                else
                    focused = null;
            focusedChanging(focused);
            this.focused = focused;
            if (oldFocused != null)
                oldFocused.getInternal().exit(focused);
            else
                exit(focused);

            List<Parent> ac = new ArrayList<Parent>();
            List<Parent> bc = new ArrayList<Parent>();

            if (oldFocused instanceof Parent)
                ac.add((Parent)oldFocused);
            Parent a = oldFocused != null ? oldFocused.getContainer() : null;
            while (a != null) {
                ac.add(a);
                a = a instanceof ParentedElement ? ((ParentedElement)a).getContainer() : null;
            }

            if (focused instanceof Parent)
                bc.add((Parent)focused);
            Parent b = focused != null ? focused.getContainer() : null;
            while (b != null) {
                bc.add(b);
                b = b instanceof ParentedElement ? ((ParentedElement)b).getContainer() : null;
            }

            int ai = ac.size() - 1;
            int bi = bc.size() - 1;

            while (ai >= 0 || bi >= 0) {
                a = ai >= 0 ? ac.get(ai) : null;
                b = bi >= 0 ? bc.get(bi) : null;

                if (a != b)
                    break;

                if (a instanceof Activable)
                    ((Activable)a).getInternal().focusChanged(focused);

                if (ai >= 0)
                    ai--;
                if (bi >= 0)
                    bi--;
            }
            if (oldFocused instanceof Activable && !(oldFocused instanceof Parent))
                ((Activable)oldFocused).getInternal().deactivate(oldFocused);
            for (int i = 0; i <= ai; i++) {
                a = ac.get(i);
                if (a instanceof Activable)
                    ((Activable)a).getInternal().deactivate(oldFocused);
            }
            for (int i = bi; i >=0; i--) {
                b = bc.get(i);
                if (b instanceof Activable)
                    ((Activable)b).getInternal().activate(focused);
            }
            if (focused instanceof Activable && !(focused instanceof Parent))
                ((Activable)focused).getInternal().activate(focused);

    //        this.focused = focused;
            if (render)
                getInternal().getRender().renderSetFocused(this);
            if (focused != null)
                focused.getInternal().enter(oldFocused);
            else
                enter(oldFocused);
            focusedChanged();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void deactivateContainer(Container container) {
        if (container == null)
            return;
        final ReentrantLock lock = this.FOCUSED_LOCK;
        lock.lock();
        try {
            Focusable focused = getFocused();
            if (container != focused && !container.isElementInHierarchy(focused))
                return;

            if (container instanceof Component)
                internalDefocusComponent((Component)container);
            else
                setFocused(null);
        } finally {
            lock.unlock();
        }
    }

    private void internalDefocusComponent(Component component) { // UNDER FOCUSED_LOCK
// TODO: Нигде не проверяется на идентияность next и component!!!          
        Container container = component.getContainer();
        Focusable next;
        if (container != null && !container.getInternal().isRemovingFromHierarchy()) {
            next = container.getChildren().getInternal().getTopAvailableForm(component);
            if (next != null) {
                setFocused(next, true, true);
                return;
            }
        }
        do {
            container = component.getParentForm();
            if (container == null)
                container = this;
            if (!container.getInternal().isRemovingFromHierarchy()) {
                next = container.getChildren().getInternal().getTopAvailableForm(component);
                if (next == null)
                    next = container.getChildren().getInternal().getNextTabStopTabable(component);
                if (next == null && container instanceof Focusable && ((Focusable)container).canFocus())
                    next = (Focusable)container;
                if (next != null) {
                    if (next instanceof Form)
                        ((Form)next).bringToFront();
                    else
                        setFocused(next, true, true);
                    return;
                }
            }
            component = container instanceof Component ? (Component)container : null;
        } while (component != null);

        setFocused(null);
    }

    public void defocusComponent(Component component) {
        final ReentrantLock lock = this.FOCUSED_LOCK;
        lock.lock();
        try {
            if (getFocused() != component)
                return;
            internalDefocusComponent(component);
        } finally {
            lock.unlock();
        }
    }

    @IgnoreProperty
    public boolean isFocus() {
        return getFocused() == null;
    }

    public void setFocus() {
        setFocused(null);
    }

    protected void focusedChanging(Focusable next) {
        for (Whiteness.FocusedChangingEvent event : onFocusedChanging)
            event.onFocusedChanging(this, next);
    }

    protected void focusedChanged() {
        for (Whiteness.FocusedChangedEvent event : onFocusedChanged)
            event.onFocusedChanged(this);
    }

    protected void enter(Focusable previous) {
        for (Whiteness.EnterEvent event : onEnter)
            event.onEnter(this, previous);
    }

    protected void exit(Focusable next) {
        for (Whiteness.ExitEvent event : onExit)
            event.onExit(this, next);
    }

    protected void contextMenu(int x, int y, EnumSet<Key.Shift> shift) throws WhitenessException, WhitenessCloseException {
        for (Whiteness.ContextMenuEvent event : onContextMenu)
            event.onContextMenu(this, x, y, shift);
    }

    protected void resize() throws WhitenessException, WhitenessCloseException {
        for (Whiteness.ResizeEvent event : onResize)
            event.onResize(this);
    }

    protected boolean previewKeyDown(Key key, boolean repeat) throws WhitenessException, WhitenessCloseException {
        for (Whiteness.PreviewKeyDownEvent event : onPreviewKeyDown)
            if (event.onPreviewKeyDown(this, key, repeat))
                return true;
        return false;
    }

    protected boolean keyDown(Key key, boolean repeat) throws WhitenessException, WhitenessCloseException {
        for (Whiteness.KeyDownEvent event : onKeyDown)
            if (event.onKeyDown(this, key, repeat))
                return true;
        return false;
    }

    protected boolean keyUp(Key key) throws WhitenessException, WhitenessCloseException {
        for (Whiteness.KeyUpEvent event : onKeyUp)
            if (event.onKeyUp(this, key))
                return true;
        return false;
    }

    protected boolean keyPress(char key, EnumSet<Key.Shift> modifier) throws WhitenessException, WhitenessCloseException {
        for (Whiteness.KeyPressEvent event : onKeyPress)
            if (event.onKeyPress(this, key, modifier))
                return true;
        return false;
    }

    public Form getActiveForm() {
        return focused != null ? focused.getParentForm() : null;
    }

    public static Whiteness getCurrentThreadWhiteness() {
        Instance instance = ApplicationFactory.getApplication().getInstanceForThread(Thread.currentThread());
        return instance != null ? instance.getWhiteness() : null;
    }

    public void join() {
        instance.join();
    }

    public void quit() {
        instance.quit();
    }

    public void reinstateCloseSequence() {
        instance.reinstateCloseSequence();
    }

    public InputStream getResourceAsStream(String resource) {
        return instance.getResourceAsStream(resource);
    }

    public URL getResource(String resource) {
        return instance.getResource(resource);
    }

    public String getTitle() {
        return instance.getTitle();
    }

    public void setTitle(String title) {
        instance.setTitle(title);
    }

    public String getIcon() {
        return instance.getIcon();
    }

    public void setIcon(String icon) {
        instance.setIcon(icon);
    }

    @Override
    protected Internal createInternal() {
        return new Internal();
    }

    @Override @IgnoreProperty
    public Internal getInternal() {
        return (Internal)internal;
    }

    public class Internal extends AbstractElement.Internal implements Container.Internal, PopupMenuEvent, AppearanceSupport, FontSupport {
        @Override
        public RenderContext getRenderContext() {
            return instance.getRenderContext();
        }

        @Override
        public WhitenessRender getRender() {
            return WhitenessRender.getInstance();
        }

        @Override
        public boolean isRemovingFromHierarchy() {
            return false;
        }

        @Override
        public void setWhiteness(Whiteness whiteness) {
//            if (whiteness == null)
//                throw new NullPointerException();
            if (whiteness != Whiteness.this)
                throw new IllegalArgumentException();
//            if (popupMenu != null)
//                whiteness.getChildren().add(popupMenu);
//            shortcutCenter.setContainer(whiteness);
            shortcutCenter.getInternal().setWhiteness(whiteness);
            super.setWhiteness(whiteness);
            children.getInternal().setWhiteness(whiteness);
        }

        public void restoreFocus(Focusable focus) {
        }

        public boolean isKeyboardCueUnderlinesShowed() {
            return keyboardCueUnderlinesShowed;
        }

        public void showKeyboardCueUnderlines() {
            if (keyboardCueUnderlinesShowed)
                return;
            keyboardCueUnderlinesShowed = true;
//            getChildren().getInternal().showKeyboardCueUnderlines();
            keyboardCueUnderlinesShowed();
        }

        private void keyboardCueUnderlinesShowed() {
            for (KeyboardCueUnderlinesSupport keyboardCueUnderlinesSupport : keyboardCueUnderlinesSupportList)
                keyboardCueUnderlinesSupport.showKeyboardCueUnderlines();
            keyboardCueUnderlinesSupportList.clear();
        }

        public boolean isFocusRectanglesShowed() {
            return focusRectanglesShowed;
        }

        public void showFocusRectangles() {
            if (focusRectanglesShowed)
                return;
            focusRectanglesShowed = true;
            if (focused != null) {
                final Element.Internal i = focused.getInternal();
                if (i instanceof ShowFocusRectanglesEvent)
                    ((ShowFocusRectanglesEvent)i).showFocusRectangles();
            }
        }

        @Override
        public void showPopupMenu(int x, int y, EnumSet<Key.Shift> shift) throws WhitenessException, WhitenessCloseException{
            contextMenu(x, y, shift);
            if (popupMenu != null)
                popupMenu.show(Whiteness.this, x, y);
        }

        @Override
        public void themeChanged() {
            getRender().renderTheme(Whiteness.this);
/*            children.getInternal().themeChanged();*/
            for (AppearanceSupport appearanceSupport : appearanceSupportList)
                appearanceSupport.themeChanged();
        }

        @Override
        public void appearanceChanged() {
            setActualFont();
            getRender().renderAppearance(Whiteness.this);
/*            children.getInternal().appearanceChanged();*/
            for (AppearanceSupport appearanceSupport : appearanceSupportList)
                appearanceSupport.appearanceChanged();
        }

        public int getElementId(Element element) {
            final ReentrantLock lock = Whiteness.this.ELEMENTS_LOCK;
            lock.lock();
            try {
                Integer id = allElementIds.get(element);
                if (id == null)
                    throw new IllegalArgumentException("Element not found in whiteness");
                return id;
            } finally {
                lock.unlock();
            }
        }

        public Element getElement(int id) {
            final ReentrantLock lock = Whiteness.this.ELEMENTS_LOCK;
            lock.lock();
            try {
                return allElements.get(id);
            } finally {
                lock.unlock();
            }
        }

        public void elementAdded(Element element) {
            final ReentrantLock lock = Whiteness.this.ELEMENTS_LOCK;
            lock.lock();
            try {
                if (!allElementIds.containsKey(element)) {
                    int id = getNextComponentId();
                    allElements.put(id, element);
                    allElementIds.put(element, id);
                }
            } finally {
                lock.unlock();
            }
        }

        public void elementRemoving(Element element) {
            if (element instanceof Component)
                defocusComponent((Component)element);
        }

        public void elementRemoved(Element element) {
            final ReentrantLock lock = Whiteness.this.ELEMENTS_LOCK;
            lock.lock();
            try {
                allElements.remove(allElementIds.get(element));
                allElementIds.remove(element);
            } finally {
                lock.unlock();
            }
        }

        private int currentComponentId = 0;

        private int getNextComponentId() {
            final int _ccId = currentComponentId;
            int result;
            do {
                result = currentComponentId++;
                if (_ccId == currentComponentId)
                    throw new WhitenessInternalError("Не могу выделить идентификатор");
            } while (allElements.get(result) != null);

            return result;
        }

        public void parentedComponentAdded(Element element) {
            Element.Internal i = element.getInternal();
            if (i instanceof AppearanceSupport)
                appearanceSupportList.add((AppearanceSupport)i);
            if (i instanceof LocaleSupport)
                localeSupportList.add((LocaleSupport)i);
            if (i instanceof KeyboardCueUnderlinesSupport)
                keyboardCueUnderlinesSupportList.add((KeyboardCueUnderlinesSupport)i);
        }

        public void parentedComponentRemoved(Element element) {
            if (element instanceof Component)
                defocusComponent((Component)element);
            Element.Internal i = element.getInternal();
            if (i instanceof AppearanceSupport)
                appearanceSupportList.remove(i);
            if (i instanceof LocaleSupport)
                localeSupportList.remove(i);
            if (!keyboardCueUnderlinesShowed && i instanceof KeyboardCueUnderlinesSupport)
                keyboardCueUnderlinesSupportList.remove(i);
        }

        public void componentHierarchyChanged(Component component) {
            //defocusComponent(component);  <-- делается в elementRemoved().
        }

        public void componentNameChanged(Named component) {
        }

        protected void resize() throws WhitenessException, WhitenessCloseException {
            Whiteness.this.resize();
            children.getInternal().resize();
        }

        @Override
        public ShortcutCenter getShortcutCenter() {
            return shortcutCenter;
        }

        @Override
        public Locale getActualLocale() {
            return locale;
        }

        private ActualFont actualFont = ActualFont.DEFAULT_FONT;

        @Override
        public ActualFont getActualFont() {
            return actualFont;
        }

        private void setActualFont() {
            ActualFont oldActualFont = actualFont;
            actualFont = ActualFont.combine(getAppearance().getTextFont(), font);
            if (!actualFont.equals(oldActualFont))
                actualFontChanged();
        }

        protected void actualFontChanged() {
            getRender().renderFont(Whiteness.this);
            children.getInternal().actualFontChanged();
        }

        @Override
        public void parentActualFontChanged() {
//            setActualFont();
        }

        protected boolean processPreviewKeyDown(Key key, boolean repeat) throws WhitenessException, WhitenessCloseException {
            return previewKeyDown(key, repeat);
        }

        protected void processKeyDown(Key key, boolean repeat) throws WhitenessException, WhitenessCloseException {
            keyDown(key, repeat);
        }

        protected void processKeyUp(Key key) throws WhitenessException, WhitenessCloseException {
            keyUp(key);
        }

        protected void processKeyPress(char key, EnumSet<Key.Shift> modifier) throws WhitenessException, WhitenessCloseException {
            keyPress(key, modifier);
        }

        @Override
        public void componentAdded(Component component) {
        }

        @Override
        public void componentRemoving(Component component) { // TODO: Может наверное быть перенесено в componentRemoved(Element element).
//            log.debug(String.format("Whiteness.componentRemoving %s(%s).", component.getClass().getSimpleName(), "w"+String.valueOf(getElementId(component))));
/*            log.debug(String.format("Whiteness.componentRemoving %s.", component.getClass().getSimpleName()));

            defocusComponent(component);*/
        }

        @Override
        public void componentRemoved(Component component) {
//            log.debug(String.format("Whiteness.componentRemoved %s(%s).", component.getClass().getSimpleName(), "w"+String.valueOf(getElementId(component))));
/*            log.debug(String.format("Whiteness.componentRemoved %s.", component.getClass().getSimpleName()));*/
        }

        @Override
        public void noParentScrollChanged() {
            getRender().renderScroll(Whiteness.this);
        }

        @Override
        public void componentBoundsChanged(Component component, boolean moved, boolean resize) {
            children.getInternal().componentBoundsChanged(component, moved, resize);
        }

        @Override
        public void componentAnchorChanged(Component component) {
        }

        @Override
        public void componentVisibleChanged(Component component) {
            children.getInternal().componentVisibleChanged(component);
        }

        @BrowserEvent(name = "onRefresh")
        public void onRefresh(int width, int height, String appName, float verion, int agent, String locale, String platform, boolean cookieEnabled, String userAgent, String os) throws WhitenessException, WhitenessCloseException {
            instance.setClientInfo(new ClientInformation(userAgent, locale, appName, verion, agent, platform, os));
            instance.invalidRenderContext(); // TODO: При запросе пустого ресурса контекст сбрасывается!!!
            rect.setRect(0, 0, width, height);
            children.getInternal().doLoyaut();
            instance.refreshRenderContext();
            if (getWhiteness() == null) {
                if (getLocale() == null) {
                    final Locale clientLocale = instance.getClientInfo().getLocale();
                    if (clientLocale != null)
                        setLocale(clientLocale);
                }
                setWhiteness(Whiteness.this);
                instance.init();
            } else {
/*                final ReentrantLock lock = Whiteness.this.ELEMENTS_LOCK;
                lock.lock();
                try {
                    for (Element element : allElementIds.keySet())
                        element.getInternal().clearRendered();
                } finally {
                    lock.unlock();
                }*/
                getRender().render(Whiteness.this);
            }
        }

        public void close() {
            Whiteness.this.closed();
        }

        @BrowserEvent(name = "onresize")
        public void onResize(int width, int height) throws WhitenessException, WhitenessCloseException {
            rect.setRect(0, 0, width, height);
            resize();
        }

        @BrowserEvent(name = "onpopupmenu")
        public void onPopupMenu(int x, int y, int modifier) throws WhitenessException, WhitenessCloseException {
           showPopupMenu(x, y, Key.shiftFromModifier(modifier));
        }

        @BrowserEvent(name = "onpreviewkeydown")
        public void onPreviewKeyDown(int keyCode, int modifier, boolean repeat) throws WhitenessException, WhitenessCloseException {
            Key.Code code = Key.Code.valueOf(keyCode);
            if (code == null)
                return;
            Key key = new Key(code, Key.shiftFromModifier(modifier));
            processPreviewKeyDown(key, repeat);
        }

        @BrowserEvent(name = "onkeydown")
        public void onKeyDown(int keyCode, int modifier, boolean repeat) throws WhitenessException, WhitenessCloseException {
            Key.Code code = Key.Code.valueOf(keyCode);
            if (code == null)
                return;
            Key key = new Key(code, Key.shiftFromModifier(modifier));
//            processPreviewKeyDown(key, repeat);
            processKeyDown(key, repeat);
        }

        @BrowserEvent(name = "onkeyup")
        public void onKeyUp(int keyCode, int modifier) throws WhitenessException, WhitenessCloseException {
            Key.Code code = Key.Code.valueOf(keyCode);
            if (code == null)
                return;
            Key key = new Key(code, Key.shiftFromModifier(modifier));
            processKeyUp(key);
//            if (key.isInputKey())
//                processKeyPress(key);
        }

        @BrowserEvent(name = "onkeypress")
        public void onKeyPress(int keyCode, int modifier) throws WhitenessException, WhitenessCloseException {
            processKeyPress((char)keyCode, Key.shiftFromModifier(modifier));
        }

        @BrowserEvent(name = "onfocused")
        public boolean onFocused(Focusable focused) {
//            return setFocused(focused);
            return setFocused(focused, false, false);
        }

        @Override
        public TabSupport getTabSupport() {
            return getChildren().getInternal();
        }
    }
}
