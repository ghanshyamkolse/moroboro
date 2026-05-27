/* ==========================================================================
   MOROBORO WEB APP - APPLICATION CONTROLLER
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
    // App State Configuration
    const state = {
        useDemoMode: true, // Default to true until we verify connection
        activeTab: 'tab-home',
        channelId: '', // Set dynamically upon Google login to support search
        accentColor: localStorage.getItem('app_accent_color') || 'youtube',
        videosList: [],
        playlistsList: [],
        currentPlaylistVideos: [],
        googleAuthenticated: false,
        googleUser: null
    };

    // Mock/Demo Data (Selected high-quality real YouTube videos for fallback)
    const demoData = {
        channelName: "Google Tech Hub (Demo)",
        subscribers: "1.42M subscribers • Verified Demo Account",
        videos: [
            {
                id: "v50-WJt39Sg",
                title: "Introducing Gemini 1.5 Pro: Our next-generation model with 1 million token context",
                description: "Gemini 1.5 Pro is our latest mid-size multimodal model. It delivers dramatic performance improvements across a wide range of tasks and introduces a breakthrough in long-context window capability. Learn about its architecture, performance on complex code analysis, video understanding, and multi-hour document reasoning.",
                publishedAt: "2024-02-15T12:00:00Z",
                views: "342,084",
                duration: "2:45",
                thumbnail: "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=600&auto=format&fit=crop&q=80"
            },
            {
                id: "XXYlFuWEuKI",
                title: "Google I/O 2024 Keynote in 12 Minutes",
                description: "Catch all the major announcements from Google I/O 2024 in this fast-paced summary. Discover the latest updates on Project Astra, Google Search innovations powered by Gemini, Android 15 features, Veo AI video generator, and Google Photos Ask Photos search utility.",
                publishedAt: "2024-05-14T18:00:00Z",
                views: "891,440",
                duration: "11:58",
                thumbnail: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80"
            },
            {
                id: "0eJVy9tOBKE",
                title: "How Google Search Works: Crawling, Indexing, and Serving Results",
                description: "Go behind the scenes of Google Search with this educational breakdown. Learn how Google discovery engines find webpages through web crawlers (Googlebot), organize them in the search index, and retrieve them in milliseconds using advanced ranking algorithms.",
                publishedAt: "2023-11-20T10:00:00Z",
                views: "522,716",
                duration: "5:21",
                thumbnail: "https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=600&auto=format&fit=crop&q=80"
            },
            {
                id: "Y-XWwSJ9CIE",
                title: "Sleek CSS Animations Tutorial: Creating Premium Web Micro-interactions",
                description: "Master CSS transitions, keyframe animations, and custom timing functions. This crash course covers glassmorphic hover scales, sliding indicators, glowing neon active states, and layout transformations for interactive mobile-first interfaces.",
                publishedAt: "2025-09-12T14:30:00Z",
                views: "45,129",
                duration: "18:40",
                thumbnail: "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=600&auto=format&fit=crop&q=80"
            },
            {
                id: "py3m_k9m7xI",
                title: "Building Glassmorphic UI Components - Step-by-Step Frontend Design",
                description: "A comprehensive guide on styling glassmorphism using pure HTML and Vanilla CSS. We explore CSS custom variables, backdrop-filter properties, box-shadow ambient glows, and layout optimizations for web and hybrid mobile applications.",
                publishedAt: "2026-01-05T09:00:00Z",
                views: "12,940",
                duration: "14:15",
                thumbnail: "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&auto=format&fit=crop&q=80"
            },
            {
                id: "2a_HCNpSEN8",
                title: "SpaceX Starship Flight Test 4 Overview and Highlights",
                description: "Relive the incredible moments of SpaceX's fourth flight test of Starship. Watch the super heavy booster splashdown in the Gulf of Mexico, orbital coast phase telemetry, and the dramatic plasma glow re-entry before soft water landing in the Indian Ocean.",
                publishedAt: "2024-06-06T15:20:00Z",
                views: "2,408,119",
                duration: "8:05",
                thumbnail: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80"
            }
        ],
        playlists: [
            {
                id: "pl-ai-gemini",
                title: "Google AI & Gemini Developments",
                count: 2,
                thumbnail: "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=600&auto=format&fit=crop&q=80",
                videos: ["v50-WJt39Sg", "XXYlFuWEuKI"]
            },
            {
                id: "pl-frontend",
                title: "Advanced Frontend UI Designs",
                count: 2,
                thumbnail: "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=600&auto=format&fit=crop&q=80",
                videos: ["Y-XWwSJ9CIE", "py3m_k9m7xI"]
            },
            {
                id: "pl-tech-science",
                title: "Space & Tech Innovations",
                count: 2,
                thumbnail: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
                videos: ["0eJVy9tOBKE", "2a_HCNpSEN8"]
            }
        ]
    };

    // YouTube Player API Variables
    let ytPlayer = null;
    let ytApiReady = false;

    // Wait for official YouTube Iframe Player API Ready
    window.onYouTubeIframeAPIReady = () => {
        ytApiReady = true;
    };

    /* ==========================================================================
       INITIALIZATION AND SETUP
       ========================================================================== */
    init();

    async function init() {
        updateClock();
        setInterval(updateClock, 1000);
        
        applyAccentTheme(state.accentColor);
        loadSettingsColors();
        
        // Fetch Google Login state
        await checkGoogleAuth();
        
        // Deciding mode and loading feeds
        resolveApplicationMode();
        loadDataFeed();
        setupEventListeners();
    }

    // Modern clock update in mobile status bar
    function updateClock() {
        const timeEl = document.getElementById('status-time');
        if (timeEl) {
            const now = new Date();
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            timeEl.textContent = `${hours}:${minutes}`;
        }
    }

    // Check Google Login status
    async function checkGoogleAuth() {
        try {
            const res = await fetch('/api/youtube/profile');
            if (res.ok) {
                const data = await res.json();
                state.googleAuthenticated = data.authenticated;
                if (data.authenticated) {
                    state.googleUser = data;
                }
            }
        } catch (e) {
            console.error("Failed to check Google Login status:", e);
            state.googleAuthenticated = false;
        }
    }

    // Render User Account Status card in settings
    function renderSettingsProfileUI() {
        const cardUnauth = document.getElementById('settings-account-unauth');
        const cardAuth = document.getElementById('settings-account-auth');

        if (!cardUnauth || !cardAuth) return;

        if (state.googleAuthenticated && state.googleUser) {
            cardUnauth.style.display = 'none';
            cardAuth.style.display = 'block';

            document.getElementById('account-profile-name').textContent = state.googleUser.name;
            document.getElementById('account-profile-email').textContent = state.googleUser.email;
            if (state.googleUser.avatar) {
                const accProfileAv = document.getElementById('account-profile-avatar');
                if (accProfileAv) accProfileAv.src = state.googleUser.avatar;
                const headerAv = document.getElementById('header-avatar');
                if (headerAv) headerAv.src = state.googleUser.avatar;
                const avLarge = document.getElementById('avatar-large');
                if (avLarge) avLarge.src = state.googleUser.avatar;
            }
        } else {
            cardUnauth.style.display = 'block';
            cardAuth.style.display = 'none';
        }
    }

    // Load Subscriptions horizontal bar bubbles
    async function loadSubscriptionsBar() {
        const container = document.getElementById('subscriptions-scroll-container');
        if (!container) return;
        container.innerHTML = '';

        try {
            const res = await fetch('/api/youtube/subscriptions');
            const data = await res.json();
            const items = data.items || [];

            if (items.length === 0) {
                container.innerHTML = '<span style="font-size:11px; color:var(--text-muted); padding: 4px 10px;">No channels found.</span>';
                return;
            }

            // Add "All Subs" combined bubble
            const allBubble = document.createElement('div');
            allBubble.className = `sub-bubble ${!state.activeSubscribedChannelId ? 'active' : ''}`;
            allBubble.innerHTML = `
                <div class="bubble-avatar-wrapper">
                    <div class="bubble-avatar" style="display:flex; align-items:center; justify-content:center; background:#1e1a2c;">
                        <i class="fa-solid fa-list-ul" style="font-size: 15px; color: var(--accent-color);"></i>
                    </div>
                </div>
                <span class="bubble-name">All Subs</span>`;
            
            allBubble.addEventListener('click', () => {
                document.querySelectorAll('.sub-bubble').forEach(b => b.classList.remove('active'));
                allBubble.classList.add('active');
                state.activeSubscribedChannelId = null;
                loadDataFeed();
            });
            container.appendChild(allBubble);

            // Add actual channels
            items.forEach(item => {
                const snippet = item.snippet;
                if (!snippet) return;
                const channelId = snippet.resourceId.channelId;
                const channelName = snippet.title;
                const thumbnail = snippet.thumbnails && snippet.thumbnails.default ? snippet.thumbnails.default.url : '/assets/avatar.png';

                const bubble = document.createElement('div');
                bubble.className = `sub-bubble ${state.activeSubscribedChannelId === channelId ? 'active' : ''}`;
                bubble.innerHTML = `
                    <div class="bubble-avatar-wrapper">
                        <img src="${thumbnail}" alt="${channelName}" class="bubble-avatar" loading="lazy">
                    </div>
                    <span class="bubble-name">${escapeHtml(channelName)}</span>`;

                bubble.addEventListener('click', () => {
                    document.querySelectorAll('.sub-bubble').forEach(b => b.classList.remove('active'));
                    bubble.classList.add('active');
                    state.activeSubscribedChannelId = channelId;
                    loadDataFeed();
                });

                container.appendChild(bubble);
            });

        } catch (e) {
            console.error("Failed to load subscription bubbles bar:", e);
            container.innerHTML = '<span style="font-size:11px; color:var(--text-muted); padding: 4px 10px;">Error loading subscriptions.</span>';
        }
    }

    // Align local config values
    function resolveApplicationMode() {
        if (state.googleAuthenticated) {
            state.useDemoMode = false;
            document.getElementById('settings-demo-toggle').checked = false;
        } else {
            state.useDemoMode = true;
            document.getElementById('settings-demo-toggle').checked = true;
        }
    }

    // Load colors selection in settings forms
    function loadSettingsColors() {
        document.querySelectorAll('.color-dot').forEach(dot => {
            if (dot.dataset.color === state.accentColor) {
                dot.classList.add('active');
            } else {
                dot.classList.remove('active');
            }
        });
    }

    /* ==========================================================================
       DATA RETRIEVAL & RENDERING
       ========================================================================== */
    
    async function loadDataFeed() {
        // Skeletons showing
        document.getElementById('feed-skeletons').style.display = 'flex';
        document.getElementById('video-feed-grid').style.display = 'none';
        document.getElementById('playlists-skeletons').style.display = 'grid';
        document.getElementById('playlists-grid').style.display = 'none';
        
        const demoBanner = document.getElementById('demo-banner-alert');
        const bubbleBar = document.getElementById('subscriptions-bubble-bar');
        
        renderSettingsProfileUI();

        if (state.googleAuthenticated) {
            demoBanner.style.display = 'none';
            bubbleBar.style.display = 'none';

            try {
                // Fetch my channel details
                const resChan = await fetch('/api/youtube/my-channel-details');
                const dataChan = await resChan.json();
                
                if (dataChan.error) {
                    throw new Error(dataChan.error);
                }

                let profileName = state.googleUser.name;
                let profileSubs = "Synced Channel";
                
                if (dataChan.items && dataChan.items.length > 0) {
                    const chan = dataChan.items[0];
                    profileName = chan.snippet.title;
                    profileSubs = formatSubscriberCount(chan.statistics.subscriberCount) + " subscribers";
                    state.channelId = chan.id; // Store channel ID for search and other views
                    
                    // Update header and banner avatar/profile pictures
                    if (chan.snippet.thumbnails && chan.snippet.thumbnails.high) {
                        const avatarUrl = chan.snippet.thumbnails.high.url;
                        const headerAv = document.getElementById('header-avatar');
                        if (headerAv) headerAv.src = avatarUrl;
                        const avLarge = document.getElementById('avatar-large');
                        if (avLarge) avLarge.src = avatarUrl;
                        const accProfileAv = document.getElementById('account-profile-avatar');
                        if (accProfileAv) accProfileAv.src = avatarUrl;
                    }
                }
                
                updateChannelUI(profileName, profileSubs);

                // Fetch uploads (authenticated /api/youtube/uploads returns user uploads)
                const resFeed = await fetch('/api/youtube/uploads');
                const dataFeed = await resFeed.json();
                
                if (dataFeed.error) {
                    throw new Error(dataFeed.error);
                }

                const feedItems = dataFeed.items || [];
                state.videosList = mapYoutubeResponse(feedItems);
                renderVideoGrid('video-feed-grid', state.videosList);

                // Fetch Playlists from authenticated API
                const resPlaylists = await fetch('/api/youtube/playlists');
                const dataPlaylists = await resPlaylists.json();
                
                if (dataPlaylists.error) {
                    throw new Error(dataPlaylists.error);
                }

                const playlistItems = dataPlaylists.items || [];
                
                state.playlistsList = playlistItems.map(p => ({
                    id: p.id,
                    title: p.snippet.title,
                    count: p.contentDetails ? p.contentDetails.itemCount : 0,
                    thumbnail: p.snippet.thumbnails && p.snippet.thumbnails.medium ? p.snippet.thumbnails.medium.url : (p.snippet.thumbnails && p.snippet.thumbnails.high ? p.snippet.thumbnails.high.url : '/assets/banner.png'),
                    videos: []
                }));
                renderPlaylistsGrid(state.playlistsList);

            } catch (e) {
                console.error("YouTube Channel Feed Fetch failed: ", e);
                bubbleBar.style.display = 'none';
                demoBanner.style.display = 'flex';
                demoBanner.querySelector('span').innerHTML = `<strong>Google API Sync Error:</strong> ${e.message}. Loading Sandbox fallback.`;
                
                updateChannelUI(demoData.channelName, demoData.subscribers);
                state.videosList = demoData.videos;
                renderVideoGrid('video-feed-grid', state.videosList);
                state.playlistsList = demoData.playlists;
                renderPlaylistsGrid(state.playlistsList);
            }

        } else if (state.useDemoMode) {
            demoBanner.style.display = 'flex';
            bubbleBar.style.display = 'none';
            
            // Channel Info Header Updates
            updateChannelUI(demoData.channelName, demoData.subscribers);

            // Feed Load
            state.videosList = demoData.videos;
            renderVideoGrid('video-feed-grid', state.videosList);
            
            // Playlists Load
            state.playlistsList = demoData.playlists;
            renderPlaylistsGrid(state.playlistsList);
        }

        // Hide Skeletons, Show elements
        document.getElementById('feed-skeletons').style.display = 'none';
        document.getElementById('video-feed-grid').style.display = 'flex';
        document.getElementById('playlists-skeletons').style.display = 'none';
        document.getElementById('playlists-grid').style.display = 'grid';
    }

    // Helper to format YouTube list response to clean application video format
    function mapYoutubeResponse(items) {
        return items.map(item => {
            const snippet = item.snippet;
            const videoId = snippet.resourceId ? snippet.resourceId.videoId : (item.id ? (item.id.videoId || item.id) : '');
            
            // Format views and duration
            const publishTime = new Date(snippet.publishedAt).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
            
            return {
                id: videoId,
                title: snippet.title,
                description: snippet.description || "No description available.",
                publishedAt: snippet.publishedAt,
                formattedDate: publishTime,
                views: "Watch Video", // playlistItems doesn't return viewCount by default, requires separate query
                duration: "Video",
                thumbnail: snippet.thumbnails && snippet.thumbnails.medium ? snippet.thumbnails.medium.url : (snippet.thumbnails && snippet.thumbnails.high ? snippet.thumbnails.high.url : '/assets/banner.png')
            };
        });
    }

    // Render videos list inside target grid element
    function renderVideoGrid(targetId, videosArray) {
        const grid = document.getElementById(targetId);
        grid.innerHTML = '';

        if (videosArray.length === 0) {
            grid.innerHTML = `
                <div class="empty-state">
                    <i class="fa-solid fa-video-slash"></i>
                    <p>No videos found in this account feed.</p>
                </div>`;
            return;
        }

        videosArray.forEach(video => {
            const card = document.createElement('article');
            card.className = 'video-card';
            card.setAttribute('data-videoid', video.id);
            card.innerHTML = `
                <div class="video-thumb-container">
                    <img src="${video.thumbnail}" alt="Video thumbnail" class="video-thumbnail" loading="lazy">
                    <span class="video-duration">${video.duration}</span>
                </div>
                <div class="video-details">
                    <img src="/assets/avatar.png" alt="Channel Avatar" class="video-channel-avatar">
                    <div class="video-meta-info">
                        <h4 class="video-title">${escapeHtml(video.title)}</h4>
                        <div class="video-sub-meta">
                            <span>${video.views}</span> • <span>${video.formattedDate || formatTimeAgo(video.publishedAt)}</span>
                        </div>
                    </div>
                </div>`;
            
            // Click handler to launch player
            card.addEventListener('click', () => {
                const cleanDate = video.formattedDate || formatTimeAgo(video.publishedAt);
                launchPlayer(video.id, video.title, cleanDate, video.views, video.description);
            });

            grid.appendChild(card);
        });
    }

    // Render Playlists View Grid
    function renderPlaylistsGrid(playlistsArray) {
        const grid = document.getElementById('playlists-grid');
        grid.innerHTML = '';

        if (playlistsArray.length === 0) {
            grid.innerHTML = `
                <div class="empty-state" style="grid-column: span 2;">
                    <i class="fa-solid fa-list-ul"></i>
                    <p>No playlists found in this account.</p>
                </div>`;
            return;
        }

        playlistsArray.forEach(playlist => {
            const card = document.createElement('div');
            card.className = 'playlist-card';
            card.innerHTML = `
                <div class="playlist-thumb-container">
                    <img src="${playlist.thumbnail}" alt="Playlist thumbnail" class="playlist-thumbnail" loading="lazy">
                    <div class="playlist-overlay-stack">
                        <div class="playlist-count-badge">
                            <i class="fa-solid fa-play"></i>
                            <span>${playlist.count} videos</span>
                        </div>
                    </div>
                </div>
                <div class="playlist-info">
                    <h4 class="playlist-title">${escapeHtml(playlist.title)}</h4>
                </div>`;
            
            card.addEventListener('click', () => {
                openPlaylistDetail(playlist);
            });

            grid.appendChild(card);
        });
    }

    // Open Playlist Videos Detail screen
    async function openPlaylistDetail(playlist) {
        const detailOverlay = document.getElementById('playlist-detail-overlay');
        const detailTitle = document.getElementById('playlist-detail-title');
        const detailGrid = document.getElementById('playlist-detail-grid');
        
        detailTitle.textContent = playlist.title;
        detailGrid.innerHTML = `
            <div class="skeleton-container" style="padding: 12px; width: 100%;">
                <div class="skeleton-line-1" style="width: 100%;"></div>
                <div class="skeleton-line-2" style="width: 70%; margin-top: 10px;"></div>
            </div>`;
        
        detailOverlay.classList.add('open');

        if (state.useDemoMode) {
            // Retrieve actual videos corresponding to the playlist
            const playlistVideoIds = playlist.videos;
            const matchedVideos = demoData.videos.filter(v => playlistVideoIds.includes(v.id));
            renderVideoGrid('playlist-detail-grid', matchedVideos);
        } else {
            try {
                const res = await fetch(`/api/youtube/playlist-items?playlistId=${playlist.id}`);
                const data = await res.json();
                const listItems = data.items || [];
                const matchedVideos = mapYoutubeResponse(listItems);
                renderVideoGrid('playlist-detail-grid', matchedVideos);
            } catch (e) {
                detailGrid.innerHTML = `
                    <div class="empty-state">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                        <p>Error loading playlist items: ${e.message}</p>
                    </div>`;
            }
        }
    }

    // Search query routing
    async function runSearch(queryText) {
        const resultsGrid = document.getElementById('search-results-grid');
        const statusText = document.getElementById('search-status-text');
        
        if (!queryText || queryText.trim() === '') {
            resultsGrid.innerHTML = '';
            statusText.textContent = 'Search for videos in your channel';
            return;
        }

        statusText.textContent = `Searching for "${queryText}"...`;
        resultsGrid.innerHTML = `
            <div class="skeleton-container" style="padding: 12px; width:100%;">
                <div class="skeleton-line-1" style="width: 90%;"></div>
                <div class="skeleton-line-2" style="width: 50%; margin-top: 10px;"></div>
            </div>`;

        if (state.useDemoMode) {
            const query = queryText.toLowerCase().trim();
            const matched = demoData.videos.filter(v => 
                v.title.toLowerCase().includes(query) || 
                v.description.toLowerCase().includes(query)
            );
            statusText.textContent = `Showing ${matched.length} results for "${queryText}"`;
            renderVideoGrid('search-results-grid', matched);
        } else {
            try {
                const res = await fetch(`/api/youtube/search?q=${encodeURIComponent(queryText)}`);
                const data = await res.json();
                const items = data.items || [];
                
                // search api returns items with id.videoId. Map it.
                const results = mapYoutubeResponse(items);
                statusText.textContent = `Showing ${results.length} results for "${queryText}"`;
                renderVideoGrid('search-results-grid', results);
            } catch (e) {
                statusText.textContent = 'Search failed';
                resultsGrid.innerHTML = `
                    <div class="empty-state">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        <p>Search error: ${e.message}</p>
                    </div>`;
            }
        }
    }

    // Utility to modify top/banner profile information
    function updateChannelUI(name, subs) {
        document.getElementById('header-channel-title').textContent = name;
        document.getElementById('header-channel-subs').textContent = subs.split('•')[0];
        document.getElementById('banner-channel-name').textContent = name;
        document.getElementById('banner-channel-subs-count').textContent = subs;
    }

    /* ==========================================================================
       YOUTUBE EMBED PLAYER LOGIC
       ========================================================================== */
    
    function launchPlayer(videoId, title, date, views, description) {
        const playerEl = document.getElementById('floating-player');
        
        // Populate view panels
        document.getElementById('playing-video-title').textContent = title;
        document.getElementById('playing-video-date').innerHTML = `<i class="fa-regular fa-calendar"></i> ${date}`;
        document.getElementById('playing-video-views').innerHTML = `<i class="fa-regular fa-eye"></i> ${views}`;
        document.getElementById('playing-video-description').textContent = description;

        // Reset display
        playerEl.classList.remove('closed');
        playerEl.classList.remove('open-pip');
        playerEl.classList.add('open-full');

        // Handle Iframe API player load or direct iframe inject
        if (ytApiReady) {
            try {
                const placeholder = document.getElementById('youtube-iframe-placeholder');
                // If player is already initialized, just load the video ID
                if (ytPlayer && typeof ytPlayer.loadVideoById === 'function') {
                    ytPlayer.loadVideoById(videoId);
                } else {
                    // Create new player
                    placeholder.innerHTML = '';
                    ytPlayer = new YT.Player('youtube-iframe-placeholder', {
                        height: '100%',
                        width: '100%',
                        videoId: videoId,
                        playerVars: {
                            'autoplay': 1,
                            'playsinline': 1,
                            'modestbranding': 1,
                            'rel': 0
                        },
                        events: {
                            'onStateChange': onPlayerStateChange
                        }
                    });
                }
            } catch (err) {
                console.error("Iframe API Player initialization failed, utilizing fallback iframe element.", err);
                injectFallbackIframe(videoId);
            }
        } else {
            injectFallbackIframe(videoId);
        }
    }

    function injectFallbackIframe(videoId) {
        const frameContainer = document.querySelector('.iframe-aspect-ratio');
        frameContainer.innerHTML = `
            <iframe id="youtube-iframe-placeholder" width="100%" height="100%" 
                src="https://www.youtube.com/embed/${videoId}?autoplay=1&playsinline=1&enablejsapi=1" 
                frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                allowfullscreen></iframe>`;
    }

    function onPlayerStateChange(event) {
        // Can be used for custom overlay controls if needed
    }

    // Toggle minimize PiP player
    function minimizePlayer() {
        const playerEl = document.getElementById('floating-player');
        playerEl.classList.remove('open-full');
        playerEl.classList.add('open-pip');
    }

    // Restore PiP player to Full Screen
    function restorePlayer() {
        const playerEl = document.getElementById('floating-player');
        playerEl.classList.remove('open-pip');
        playerEl.classList.add('open-full');
    }

    // Close Player drawer completely
    function closePlayer() {
        const playerEl = document.getElementById('floating-player');
        playerEl.classList.remove('open-full');
        playerEl.classList.remove('open-pip');
        playerEl.classList.add('closed');
        
        // Stop YouTube video playing
        if (ytPlayer && typeof ytPlayer.stopVideo === 'function') {
            ytPlayer.stopVideo();
        } else {
            const iframe = document.getElementById('youtube-iframe-placeholder');
            if (iframe) {
                iframe.src = '';
            }
        }
    }

    /* ==========================================================================
       THEMING AND STYLES
       ========================================================================== */

    function applyAccentTheme(colorName) {
        // Clear old classes
        document.body.className = '';
        
        // Add new class
        const validColors = ['youtube', 'purple', 'cyan', 'emerald', 'amber'];
        const activeColor = validColors.includes(colorName) ? colorName : 'youtube';
        document.body.classList.add(`theme-${activeColor}`);
        
        state.accentColor = activeColor;
        localStorage.setItem('app_accent_color', activeColor);
        
        // Sync active state in UI settings dots
        document.querySelectorAll('.color-dot').forEach(dot => {
            if (dot.dataset.color === activeColor) {
                dot.classList.add('active');
            } else {
                dot.classList.remove('active');
            }
        });
        
        // Trigger page glow colors repaint
        const glows = document.querySelectorAll('.bg-glow-1');
        if (glows.length > 0) {
            glows[0].style.backgroundColor = 'var(--accent-color)';
        }
    }

    /* ==========================================================================
       EVENT LISTENERS HANDLERS
       ========================================================================== */
    
    function setupEventListeners() {
        // Bottom Navigation Tab Clicks
        document.querySelectorAll('.nav-item').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const targetTab = btn.getAttribute('data-target');
                switchTab(targetTab);
            });
        });

        // App Header Shortcuts
        document.getElementById('header-search-trigger').addEventListener('click', () => {
            switchTab('tab-search');
            document.getElementById('search-input-field').focus();
        });

        document.getElementById('header-settings-trigger').addEventListener('click', () => {
            switchTab('tab-settings');
        });

        // Refresh Feed Button
        document.getElementById('btn-refresh-feed').addEventListener('click', () => {
            loadDataFeed();
        });

        // Search Input Handlers
        const searchInput = document.getElementById('search-input-field');
        const searchClear = document.getElementById('search-clear-btn');
        let searchDebounceTimer;

        searchInput.addEventListener('input', (e) => {
            const val = e.target.value;
            
            // Clear icon toggler
            if (val.length > 0) {
                searchClear.style.display = 'block';
            } else {
                searchClear.style.display = 'none';
            }

            // Debounced API search triggers
            clearTimeout(searchDebounceTimer);
            searchDebounceTimer = setTimeout(() => {
                runSearch(val);
            }, 500);
        });

        searchClear.addEventListener('click', () => {
            searchInput.value = '';
            searchClear.style.display = 'none';
            runSearch('');
            searchInput.focus();
        });

        // Back to Playlists Button
        document.getElementById('playlist-detail-back').addEventListener('click', () => {
            document.getElementById('playlist-detail-overlay').classList.remove('open');
        });

        // Toggle Demo Mode Switch Checkbox
        document.getElementById('settings-demo-toggle').addEventListener('change', (e) => {
            const isChecked = e.target.checked;
            state.useDemoMode = isChecked;
            localStorage.setItem('settings_demo_toggle', isChecked ? 'true' : 'false');
            
            loadDataFeed();
            switchTab('tab-home');
        });

        // Tab switching for Login / Register form
        const btnShowLogin = document.getElementById('btn-show-login');
        const btnShowRegister = document.getElementById('btn-show-register');
        const formLogin = document.getElementById('form-login-container');
        const formRegister = document.getElementById('form-register-container');
        const authFeedback = document.getElementById('auth-feedback-message');

        if (btnShowLogin && btnShowRegister) {
            btnShowLogin.addEventListener('click', () => {
                btnShowLogin.classList.add('active');
                btnShowLogin.style.color = 'var(--accent-color)';
                btnShowLogin.style.borderBottom = '2px solid var(--accent-color)';
                
                btnShowRegister.classList.remove('active');
                btnShowRegister.style.color = 'var(--text-muted)';
                btnShowRegister.style.borderBottom = 'none';
                
                formLogin.style.display = 'block';
                formRegister.style.display = 'none';
                authFeedback.textContent = '';
            });

            btnShowRegister.addEventListener('click', () => {
                btnShowRegister.classList.add('active');
                btnShowRegister.style.color = 'var(--accent-color)';
                btnShowRegister.style.borderBottom = '2px solid var(--accent-color)';
                
                btnShowLogin.classList.remove('active');
                btnShowLogin.style.color = 'var(--text-muted)';
                btnShowLogin.style.borderBottom = 'none';
                
                formRegister.style.display = 'block';
                formLogin.style.display = 'none';
                authFeedback.textContent = '';
            });
        }

        // Submit Sign In handler
        const btnSubmitLogin = document.getElementById('btn-submit-login');
        if (btnSubmitLogin) {
            btnSubmitLogin.addEventListener('click', async () => {
                const email = document.getElementById('login-email').value.trim();
                const password = document.getElementById('login-password').value;

                if (!email || !password) {
                    authFeedback.className = 'settings-feedback error';
                    authFeedback.style.color = '#ff4d4d';
                    authFeedback.textContent = 'Please enter email and password.';
                    return;
                }

                authFeedback.className = 'settings-feedback';
                authFeedback.style.color = 'var(--text-muted)';
                authFeedback.textContent = 'Signing in...';

                try {
                    const formData = new URLSearchParams();
                    formData.append('email', email);
                    formData.append('password', password);

                    const res = await fetch('/api/auth/login', {
                        method: 'POST',
                        body: formData
                    });
                    const data = await res.json();

                    if (data.error) {
                        authFeedback.style.color = '#ff4d4d';
                        authFeedback.textContent = data.error;
                    } else {
                        authFeedback.style.color = '#06d6a0';
                        authFeedback.textContent = 'Sign in successful!';
                        state.googleAuthenticated = true;
                        state.googleUser = data;

                        setTimeout(() => {
                            authFeedback.textContent = '';
                            loadDataFeed();
                            switchTab('tab-home');
                        }, 1000);
                    }
                } catch (err) {
                    authFeedback.style.color = '#ff4d4d';
                    authFeedback.textContent = 'Connection error. Please try again.';
                }
            });
        }

        // Submit Register handler
        const btnSubmitRegister = document.getElementById('btn-submit-register');
        if (btnSubmitRegister) {
            btnSubmitRegister.addEventListener('click', async () => {
                const email = document.getElementById('register-email').value.trim();
                const password = document.getElementById('register-password').value;
                const youtubeHandle = document.getElementById('register-handle').value.trim();

                if (!email || !password || !youtubeHandle) {
                    authFeedback.className = 'settings-feedback error';
                    authFeedback.style.color = '#ff4d4d';
                    authFeedback.textContent = 'All fields are required.';
                    return;
                }

                if (password.length < 6) {
                    authFeedback.style.color = '#ff4d4d';
                    authFeedback.textContent = 'Password must be at least 6 characters.';
                    return;
                }

                authFeedback.className = 'settings-feedback';
                authFeedback.style.color = 'var(--text-muted)';
                authFeedback.textContent = 'Registering & resolving channel handle...';

                try {
                    const formData = new URLSearchParams();
                    formData.append('email', email);
                    formData.append('password', password);
                    formData.append('youtubeHandle', youtubeHandle);

                    const res = await fetch('/api/auth/register', {
                        method: 'POST',
                        body: formData
                    });
                    const data = await res.json();

                    if (data.error) {
                        authFeedback.style.color = '#ff4d4d';
                        authFeedback.textContent = data.error;
                    } else {
                        authFeedback.style.color = '#06d6a0';
                        authFeedback.textContent = 'Registration successful! Please sign in.';
                        
                        // Clear form inputs
                        document.getElementById('register-email').value = '';
                        document.getElementById('register-password').value = '';
                        document.getElementById('register-handle').value = '';

                        // Auto switch to login tab
                        setTimeout(() => {
                            btnShowLogin.click();
                            document.getElementById('login-email').value = email;
                        }, 1500);
                    }
                } catch (err) {
                    authFeedback.style.color = '#ff4d4d';
                    authFeedback.textContent = 'Connection error. Please try again.';
                }
            });
        }

        // Logout handler
        const btnAccountLogout = document.getElementById('btn-account-logout');
        if (btnAccountLogout) {
            btnAccountLogout.addEventListener('click', async () => {
                try {
                    await fetch('/api/auth/logout', { method: 'POST' });
                    state.googleAuthenticated = false;
                    state.googleUser = null;
                    state.channelId = '';

                    // Reset header/banner avatars to default fallback
                    const headerAv = document.getElementById('header-avatar');
                    if (headerAv) headerAv.src = '/assets/avatar.png';
                    const avLarge = document.getElementById('avatar-large');
                    if (avLarge) avLarge.src = '/assets/avatar.png';
                    const accProfileAv = document.getElementById('account-profile-avatar');
                    if (accProfileAv) accProfileAv.src = '/assets/avatar.png';

                    loadDataFeed();
                    switchTab('tab-home');
                } catch (err) {
                    console.error('Logout failed:', err);
                }
            });
        }

        // UI Accent colors select handler
        document.querySelectorAll('.color-dot').forEach(dot => {
            dot.addEventListener('click', () => {
                const color = dot.getAttribute('data-color');
                applyAccentTheme(color);
            });
        });

        // Minimizable floating player controls clicks
        document.getElementById('player-minimize-btn').addEventListener('click', (e) => {
            e.stopPropagation();
            minimizePlayer();
        });

        document.getElementById('player-expand-btn').addEventListener('click', (e) => {
            e.stopPropagation();
            restorePlayer();
        });

        document.getElementById('player-close-btn').addEventListener('click', (e) => {
            e.stopPropagation();
            closePlayer();
        });

        // Clicking the header bar of minimized PiP player expands it
        document.getElementById('floating-player').addEventListener('click', (e) => {
            const player = document.getElementById('floating-player');
            if (player.classList.contains('open-pip')) {
                restorePlayer();
            }
        });
    }

    // Switch between panels
    function switchTab(tabId) {
        state.activeTab = tabId;
        
        // Hide all panels
        document.querySelectorAll('.tab-panel').forEach(panel => {
            panel.classList.remove('active');
        });
        
        // Show target panel
        const targetPanel = document.getElementById(tabId);
        if (targetPanel) {
            targetPanel.classList.add('active');
        }

        // Close playlist detail sub-panel if we leave the playlists tab
        if (tabId !== 'tab-playlists') {
            document.getElementById('playlist-detail-overlay').classList.remove('open');
        }

        // Sync active state in Bottom Nav Buttons
        document.querySelectorAll('.nav-item').forEach(btn => {
            const target = btn.getAttribute('data-target');
            if (target === tabId) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });

        // Scroll app body to top on switch
        document.getElementById('app-main').scrollTop = 0;
    }

    /* ==========================================================================
       UTILITIES & FORMATTERS
       ========================================================================== */

    // HTML escape to prevent injection
    function escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    // Format ISO Dates into 'time ago' strings
    function formatTimeAgo(isoString) {
        if (!isoString) return '';
        const now = new Date();
        const past = new Date(isoString);
        const msPerMinute = 60 * 1000;
        const msPerHour = msPerMinute * 60;
        const msPerDay = msPerHour * 24;
        const msPerMonth = msPerDay * 30;
        const msPerYear = msPerDay * 365;

        const elapsed = now - past;

        if (elapsed < msPerMinute) {
             return 'Just now';   
        } else if (elapsed < msPerHour) {
             const m = Math.round(elapsed/msPerMinute);
             return m + (m === 1 ? ' minute ago' : ' minutes ago');   
        } else if (elapsed < msPerDay ) {
             const h = Math.round(elapsed/msPerHour);
             return h + (h === 1 ? ' hour ago' : ' hours ago');   
        } else if (elapsed < msPerMonth) {
            const d = Math.round(elapsed/msPerDay);
            return d + (d === 1 ? ' day ago' : ' days ago');   
        } else if (elapsed < msPerYear) {
            const m = Math.round(elapsed/msPerMonth);
            return m + (m === 1 ? ' month ago' : ' months ago');   
        } else {
            const y = Math.round(elapsed/msPerYear);
            return y + (y === 1 ? ' year ago' : ' years ago');   
        }
    }

    // Humanize subscriber numbers (e.g. 1420100 -> "1.42M")
    function formatSubscriberCount(numStr) {
        if (!numStr) return '';
        const num = parseInt(numStr, 10);
        if (isNaN(num)) return numStr;
        if (num >= 1000000) {
            return (num / 1000000).toFixed(2).replace(/\.00$/, '') + 'M';
        }
        if (num >= 1000) {
            return (num / 1000).toFixed(1).replace(/\.0$/, '') + 'K';
        }
        return num.toString();
    }
});
