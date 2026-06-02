import { useEffect, useMemo, useState } from "react";
import { authHeaders, request } from "./api";

const AUTH_STORAGE_KEY = "blog.auth";
const DRAFT_STORAGE_KEY = "blog.postDraft";

const emptyDraft = { title: "", content: "" };
const emptyLogin = { email: "", password: "" };
const emptyRegister = { email: "", nickname: "", password: "" };

function loadAuth() {
  const legacyToken = localStorage.getItem("accessToken");
  const legacyNickname = localStorage.getItem("userNickname");
  const legacyUserId = localStorage.getItem("userId");

  try {
    const saved = JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY));
    if (saved?.token && saved?.nickname && saved?.userId) return saved;
  } catch {
    // Fall back to the keys used by the previous static HTML.
  }

  return legacyToken && legacyNickname && legacyUserId
    ? { token: legacyToken, nickname: legacyNickname, userId: Number(legacyUserId) }
    : null;
}

function loadDraft() {
  try {
    const saved = JSON.parse(localStorage.getItem(DRAFT_STORAGE_KEY));
    if (saved?.title || saved?.content) return { ...emptyDraft, ...saved };
  } catch {
    // Fall back to the keys used by templates/post/write.html.
  }

  return {
    title: localStorage.getItem("draftTitle") || "",
    content: localStorage.getItem("draftContent") || ""
  };
}

function saveAuth(user) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user));
}

function clearAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY);
  localStorage.removeItem("accessToken");
  localStorage.removeItem("userId");
  localStorage.removeItem("userNickname");
}

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString("ko-KR") : "";
}

function formatDateTime(value) {
  return value ? new Date(value).toLocaleString("ko-KR") : "";
}

export default function App() {
  const [user, setUser] = useState(loadAuth);
  const [posts, setPosts] = useState([]);
  const [comments, setComments] = useState([]);
  const [selectedPostId, setSelectedPostId] = useState(null);
  const [loadingPosts, setLoadingPosts] = useState(true);
  const [postScope, setPostScope] = useState("all");
  const [modal, setModal] = useState(null);
  const [message, setMessage] = useState(null);
  const [loginForm, setLoginForm] = useState(emptyLogin);
  const [registerForm, setRegisterForm] = useState(emptyRegister);
  const [draft, setDraft] = useState(loadDraft);
  const [commentContent, setCommentContent] = useState("");
  const [formError, setFormError] = useState("");

  const selectedPost = useMemo(
    () => posts.find((post) => post.postId === selectedPostId),
    [posts, selectedPostId]
  );

  const postComments = useMemo(() => {
    if (!selectedPost) return [];
    return comments.filter(
      (comment) => comment.title?.trim() === selectedPost.title?.trim()
    );
  }, [comments, selectedPost]);

  useEffect(() => {
    loadPosts("all");
    loadComments();
  }, []);

  useEffect(() => {
    localStorage.setItem(DRAFT_STORAGE_KEY, JSON.stringify(draft));
  }, [draft]);

  useEffect(() => {
    if (!message) return undefined;
    const timer = window.setTimeout(() => setMessage(null), 4000);
    return () => window.clearTimeout(timer);
  }, [message]);

  async function loadPosts(scope = postScope) {
    setLoadingPosts(true);
    try {
      const path = scope === "mine" ? "/posts/me/user" : "/posts";
      const nextPosts = await request(path, {
        headers: scope === "mine" ? authHeaders(user?.token) : {}
      });
      setPosts(nextPosts || []);
      setPostScope(scope);
    } catch (error) {
      setMessage({ type: "error", text: `글을 불러올 수 없습니다: ${error.message}` });
      setPosts([]);
    } finally {
      setLoadingPosts(false);
    }
  }

  async function loadComments() {
    try {
      const nextComments = await request("/comments");
      setComments(nextComments || []);
    } catch {
      setComments([]);
    }
  }

  function requireLogin() {
    if (user) return true;
    setMessage({ type: "error", text: "로그인이 필요합니다." });
    setModal("login");
    return false;
  }

  function openModal(nextModal) {
    setFormError("");
    setModal(nextModal);
  }

  function closeModal() {
    setModal(null);
    setFormError("");
    setCommentContent("");
    if (modal === "post") setSelectedPostId(null);
  }

  async function loginWith(email, password) {
    const loginData = await request("/auth/login", {
      method: "POST",
      body: { email, password }
    });
    const me = await request("/users/me", {
      headers: authHeaders(loginData.accessToken)
    });
    const nextUser = {
      userId: me.userId,
      nickname: me.nickname,
      token: loginData.accessToken
    };
    setUser(nextUser);
    saveAuth(nextUser);
  }

  async function handleLogin(event) {
    event.preventDefault();
    if (!loginForm.email || !loginForm.password) {
      setFormError("이메일과 비밀번호를 입력해주세요.");
      return;
    }

    try {
      await loginWith(loginForm.email, loginForm.password);
      setLoginForm(emptyLogin);
      closeModal();
      setMessage({ type: "success", text: "로그인되었습니다." });
    } catch {
      setFormError("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    const { email, nickname, password } = registerForm;
    if (!email || !nickname || !password) {
      setFormError("모든 필드를 입력해주세요.");
      return;
    }

    try {
      await request("/users/join", {
        method: "POST",
        body: { email, nickname, password }
      });
      await loginWith(email, password);
      setRegisterForm(emptyRegister);
      closeModal();
      setMessage({ type: "success", text: "회원가입이 완료되었습니다." });
    } catch (error) {
      setFormError(`회원가입 실패: ${error.message}`);
    }
  }

  function logout() {
    setUser(null);
    clearAuth();
    setPostScope("all");
    loadPosts("all");
    setMessage({ type: "success", text: "로그아웃되었습니다." });
  }

  async function handleCreatePost(event) {
    event.preventDefault();
    if (!requireLogin()) return;
    if (!draft.title.trim() || !draft.content.trim()) {
      setFormError("제목과 내용을 모두 입력해주세요.");
      return;
    }

    try {
      await request("/posts/me", {
        method: "POST",
        headers: authHeaders(user.token),
        body: { title: draft.title.trim(), content: draft.content.trim() }
      });
      setDraft(emptyDraft);
      localStorage.removeItem(DRAFT_STORAGE_KEY);
      localStorage.removeItem("draftTitle");
      localStorage.removeItem("draftContent");
      closeModal();
      await loadPosts("all");
      setMessage({ type: "success", text: "글이 작성되었습니다." });
    } catch (error) {
      setFormError(`작성 실패: ${error.message}`);
    }
  }

  async function togglePostLike(postId) {
    if (!requireLogin()) return;
    try {
      await request(`/posts/me/${postId}/like`, {
        method: "POST",
        headers: authHeaders(user.token)
      });
      await loadPosts(postScope);
    } catch (error) {
      setMessage({ type: "error", text: `좋아요 처리 실패: ${error.message}` });
    }
  }

  async function toggleCommentLike(commentId) {
    if (!requireLogin()) return;
    try {
      await request(`/comments/me/${commentId}/like`, {
        method: "POST",
        headers: authHeaders(user.token)
      });
      await loadComments();
    } catch (error) {
      setMessage({ type: "error", text: `좋아요 처리 실패: ${error.message}` });
    }
  }

  async function createComment(event) {
    event.preventDefault();
    if (!requireLogin() || !selectedPost) return;
    if (!commentContent.trim()) {
      setMessage({ type: "error", text: "댓글 내용을 입력해주세요." });
      return;
    }

    try {
      await request(`/comments/me/${selectedPost.postId}`, {
        method: "POST",
        headers: authHeaders(user.token),
        body: { content: commentContent.trim() }
      });
      setCommentContent("");
      await loadComments();
      setMessage({ type: "success", text: "댓글이 작성되었습니다." });
    } catch (error) {
      setMessage({ type: "error", text: `댓글 작성 실패: ${error.message}` });
    }
  }

  async function deletePost(postId) {
    if (!requireLogin() || !window.confirm("정말 이 게시글을 삭제하시겠습니까?")) return;
    try {
      await request(`/posts/me/${postId}`, {
        method: "DELETE",
        headers: authHeaders(user.token)
      });
      closeModal();
      await loadPosts(postScope === "mine" ? "mine" : "all");
      setMessage({ type: "success", text: "게시글이 삭제되었습니다." });
    } catch (error) {
      setMessage({ type: "error", text: `게시글 삭제 실패: ${error.message}` });
    }
  }

  async function deleteComment(commentId) {
    if (!requireLogin() || !window.confirm("정말 이 댓글을 삭제하시겠습니까?")) return;
    try {
      await request(`/comments/me/${commentId}`, {
        method: "DELETE",
        headers: authHeaders(user.token)
      });
      await loadComments();
      setMessage({ type: "success", text: "댓글이 삭제되었습니다." });
    } catch (error) {
      setMessage({ type: "error", text: `댓글 삭제 실패: ${error.message}` });
    }
  }

  function openPost(postId) {
    setSelectedPostId(postId);
    openModal("post");
    loadComments();
  }

  async function showMyPosts() {
    if (!requireLogin()) return;
    await loadPosts("mine");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function showAllPosts() {
    await loadPosts("all");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  return (
    <>
      <header>
        <div className="nav">
          <div className="brand">
            <div aria-hidden="true" className="logo" />
            <div>Blog Project</div>
          </div>

          <nav aria-label="메인 메뉴" className="navlinks">
            <button type="button" onClick={showAllPosts}>전체 글 목록</button>
            <button type="button" onClick={showMyPosts}>내 글 목록</button>
            {user && <button type="button" onClick={() => openModal("create")}>글 작성</button>}
            <div className="user-info">
              {user ? (
                <>
                  <span>{user.nickname}</span>
                  <button type="button" onClick={logout}>로그아웃</button>
                </>
              ) : (
                <>
                  <span>로그인 필요</span>
                  <button type="button" onClick={() => openModal("login")}>로그인</button>
                  <button type="button" onClick={() => openModal("register")}>회원가입</button>
                </>
              )}
            </div>
          </nav>
        </div>
      </header>

      <div className="wrap">
        {message && <div className={`message ${message.type}`}>{message.text}</div>}

        <section className="hero">
          <h1>Blog Project</h1>
        </section>

        <main>
          <section aria-label="글 목록" className="card" id="posts">
            <div className="card-header">
              <h2>{postScope === "mine" ? "내 글" : "최근 글"}</h2>
              <span>{posts.length} post{posts.length === 1 ? "" : "s"}</span>
            </div>
            <div className="card-body">
              {loadingPosts ? (
                <div className="loading">
                  <div className="spinner" />
                  <div className="loading-text">글을 불러오는 중...</div>
                </div>
              ) : posts.length === 0 ? (
                <div className="empty">아직 작성된 글이 없습니다.</div>
              ) : (
                posts.map((post) => (
                  <article className="post" key={post.postId}>
                    <div className="meta">
                      <span className="badge">{post.authorNickname}</span>
                      <span>{formatDate(post.createdAt)}</span>
                      <span>·</span>
                      <span>좋아요 {post.likeCount || 0}</span>
                      {user && (
                        <button
                          className="btn like compact"
                          type="button"
                          onClick={() => togglePostLike(post.postId)}
                        >
                          좋아요
                        </button>
                      )}
                    </div>
                    <button className="post-row" type="button" onClick={() => openPost(post.postId)}>
                      <h3>{post.title}</h3>
                      <div className="post-excerpt">{post.content || ""}</div>
                    </button>
                  </article>
                ))
              )}
            </div>
          </section>

          <aside aria-label="사이드바" className="card">
            <div className="card-header">
              <h2>정보</h2>
              <span>Info</span>
            </div>
            <div className="card-body">
              <div className="info-text">
                <p><strong>사용 방법:</strong></p>
                <ol>
                  <li>회원가입을 진행하세요</li>
                  <li>로그인하면 글을 작성할 수 있습니다</li>
                  <li>글을 클릭하면 상세 내용과 댓글을 볼 수 있습니다</li>
                  <li>좋아요 버튼으로 글과 댓글에 반응하세요</li>
                </ol>
              </div>
            </div>
          </aside>
        </main>

        <div className="footer">
          © {new Date().getFullYear()} Blog Project. All rights reserved.
        </div>
      </div>

      {modal === "post" && selectedPost && (
        <Modal title={selectedPost.title} onClose={closeModal}>
          <div className="post-detail">
            <div className="meta">
              <span className="badge">{selectedPost.authorNickname}</span>
              <span>{formatDateTime(selectedPost.createdAt)}</span>
              <span>·</span>
              <span>좋아요 {selectedPost.likeCount || 0}</span>
            </div>
            <div className="post-content">{selectedPost.content}</div>
            <div className="actions detail-actions">
              {user && (
                <button className="btn like" type="button" onClick={() => togglePostLike(selectedPost.postId)}>
                  좋아요
                </button>
              )}
              {user?.nickname === selectedPost.authorNickname && (
                <button className="btn danger" type="button" onClick={() => deletePost(selectedPost.postId)}>
                  삭제
                </button>
              )}
            </div>
          </div>

          <section className="comments-section">
            <h4>댓글</h4>
            {postComments.length === 0 ? (
              <div className="empty">아직 댓글이 없습니다.</div>
            ) : (
              postComments.map((comment) => {
                const isMyComment = user?.nickname === comment.authorNickname;
                return (
                  <div className="comment" key={comment.commentId}>
                    <div className="comment-header">
                      <span className="comment-author">{comment.authorNickname}</span>
                      <span className="comment-date">{formatDateTime(comment.createdAt)}</span>
                    </div>
                    <div className="comment-content">{comment.content}</div>
                    <div className="comment-actions">
                      {user ? (
                        <button className="btn like small" type="button" onClick={() => toggleCommentLike(comment.commentId)}>
                          좋아요 {comment.likeCount || 0}
                        </button>
                      ) : (
                        <span className="muted-small">좋아요 {comment.likeCount || 0}</span>
                      )}
                      {isMyComment && (
                        <button className="btn danger small" type="button" onClick={() => deleteComment(comment.commentId)}>
                          삭제
                        </button>
                      )}
                    </div>
                  </div>
                );
              })
            )}

            <form className="comment-form" onSubmit={createComment}>
              <div className="form-group">
                <textarea
                  placeholder="댓글을 입력하세요..."
                  rows="3"
                  value={commentContent}
                  onChange={(event) => setCommentContent(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" && !event.shiftKey) {
                      event.preventDefault();
                      createComment(event);
                    }
                  }}
                />
              </div>
              <button className="btn primary" type="submit">댓글 작성</button>
            </form>
          </section>
        </Modal>
      )}

      {modal === "create" && (
        <Modal title="새 글 작성" onClose={closeModal}>
          <form onSubmit={handleCreatePost}>
            {formError && <div className="message error">{formError}</div>}
            <div className="form-group">
              <label htmlFor="postTitle">제목</label>
              <input
                id="postTitle"
                placeholder="글 제목을 입력하세요"
                type="text"
                value={draft.title}
                onChange={(event) => setDraft((prev) => ({ ...prev, title: event.target.value }))}
              />
            </div>
            <div className="form-group">
              <label htmlFor="postContentInput">내용</label>
              <textarea
                id="postContentInput"
                placeholder="글 내용을 입력하세요"
                rows="8"
                value={draft.content}
                onChange={(event) => setDraft((prev) => ({ ...prev, content: event.target.value }))}
              />
            </div>
            <button className="btn primary" type="submit">작성하기</button>
          </form>
        </Modal>
      )}

      {modal === "register" && (
        <Modal title="회원가입" onClose={closeModal}>
          <form onSubmit={handleRegister}>
            {formError && <div className="message error">{formError}</div>}
            <div className="form-group">
              <label htmlFor="registerEmail">이메일</label>
              <input
                id="registerEmail"
                placeholder="이메일을 입력하세요"
                type="email"
                value={registerForm.email}
                onChange={(event) => setRegisterForm((prev) => ({ ...prev, email: event.target.value }))}
              />
            </div>
            <div className="form-group">
              <label htmlFor="registerNickname">닉네임</label>
              <input
                id="registerNickname"
                placeholder="닉네임을 입력하세요"
                type="text"
                value={registerForm.nickname}
                onChange={(event) => setRegisterForm((prev) => ({ ...prev, nickname: event.target.value }))}
              />
            </div>
            <div className="form-group">
              <label htmlFor="registerPassword">비밀번호</label>
              <input
                id="registerPassword"
                placeholder="비밀번호를 입력하세요"
                type="password"
                value={registerForm.password}
                onChange={(event) => setRegisterForm((prev) => ({ ...prev, password: event.target.value }))}
              />
            </div>
            <button className="btn primary" type="submit">회원가입</button>
          </form>
        </Modal>
      )}

      {modal === "login" && (
        <Modal title="로그인" onClose={closeModal}>
          <form onSubmit={handleLogin}>
            {formError && <div className="message error">{formError}</div>}
            <div className="form-group">
              <label htmlFor="loginEmail">이메일</label>
              <input
                id="loginEmail"
                placeholder="이메일을 입력하세요"
                type="email"
                value={loginForm.email}
                onChange={(event) => setLoginForm((prev) => ({ ...prev, email: event.target.value }))}
              />
            </div>
            <div className="form-group">
              <label htmlFor="loginPassword">비밀번호</label>
              <input
                id="loginPassword"
                placeholder="비밀번호를 입력하세요"
                type="password"
                value={loginForm.password}
                onChange={(event) => setLoginForm((prev) => ({ ...prev, password: event.target.value }))}
              />
            </div>
            <button className="btn primary" type="submit">로그인</button>
          </form>
        </Modal>
      )}
    </>
  );
}

function Modal({ title, children, onClose }) {
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        aria-modal="true"
        className="modal"
        role="dialog"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="modal-head">
          <h3>{title}</h3>
          <button aria-label="닫기" className="close" type="button" onClick={onClose}>닫기</button>
        </div>
        <div className="modal-body">{children}</div>
      </section>
    </div>
  );
}
