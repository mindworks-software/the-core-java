# Versioning Convention for the-core-java

**Project:** the-core-java
**Current Version:** 4.0.0-SNAPSHOT
**Date:** 2025-10-28

---

## 📋 Overview

This document defines the versioning convention for the-core-java project, covering how versions are generated for different branch types and release stages.

---

## 🎯 Versioning Rules

### **1. Git Tags (Production Releases)**

**Format:** `{version}` (NO SHA appended)

**Examples:**
- `4.0.0`
- `4.0.1`
- `v4.0.0` (with v prefix)

**When Used:**
- Production releases only
- Tagged commits on `main` branch after successful RC testing
- Immutable, represents stable production-ready code

**Artifact Examples:**
```
Maven: com.korwe:thecore-api:4.0.0
Docker: thecore-router:4.0.0
```

**Convention:**
- ✅ Clean version number, NO commit SHA
- ✅ Suitable for production deployment
- ✅ Follows semantic versioning: MAJOR.MINOR.PATCH

---

### **2. Release Branches (Release Candidates)**

**Format:** `{version}-RC-{sha}`

**Branch Pattern:** `release-*`

**Examples:**
- Branch: `release-4.0.0`
- Version: `4.0.0-RC-abc1234`
- Branch: `release-4.0.1`
- Version: `4.0.1-RC-def5678`

**When Used:**
- Testing release candidates before tagging
- Pre-production validation
- Allows multiple RC builds from same release branch

**Artifact Examples:**
```
Maven: com.korwe:thecore-api:4.0.0-RC-abc1234
Docker: thecore-router:4.0.0-RC-abc1234
```

**Convention:**
- ✅ Includes RC (Release Candidate) suffix
- ✅ Includes 7-character commit SHA for traceability
- ✅ SHA allows multiple RC builds from same branch
- ✅ NOT suitable for production (use Git tag for production)

---

### **3. QA Branches (Testing)**

**Format:** `{branch-name}-QA-{sha}`

**Branch Pattern:** `qa/*`

**Examples:**
- Branch: `qa/full-pipeline-test`
- Version: `full-pipeline-test-QA-abc1234`
- Branch: `qa/feature-xyz`
- Version: `feature-xyz-QA-def5678`

**When Used:**
- Feature testing before merging to main
- Integration testing
- QA validation of specific features

**Artifact Examples:**
```
Maven: com.korwe:thecore-api:full-pipeline-test-QA-abc1234
Docker: thecore-router:full-pipeline-test-QA-abc1234
```

**Convention:**
- ✅ Includes QA suffix for identification
- ✅ Includes 7-character commit SHA for traceability
- ✅ Branch name becomes part of version
- ✅ NOT suitable for production

---

### **4. Other Branches (Development/Nightly Builds)**

**Format:** `4.0.0-SNAPSHOT` (FIXED, never changes)

**Examples:**
- `4.0.0-SNAPSHOT` (all development branches)
- `4.0.0-SNAPSHOT` (nightly builds)
- `4.0.0-SNAPSHOT` (feature branches)

**When Used:**
- Feature branches
- `main` branch between releases
- Any branch not matching release-* or qa/* patterns
- Nightly/continuous builds

**Artifact Examples:**
```
Maven: com.korwe:thecore-api:4.0.0-SNAPSHOT
Docker: thecore-router:4.0.0-SNAPSHOT
```

**Convention:**
- ✅ SNAPSHOT suffix indicates development/unstable
- ✅ Version is FIXED at 4.0.0-SNAPSHOT (never incremented)
- ✅ Maven treats SNAPSHOT specially (can be overwritten on each build)
- ✅ Avoids maintenance burden of updating version everywhere
- ✅ NOT suitable for production

**Why Fixed SNAPSHOT?**
- Eliminates need to update version numbers after each release
- SNAPSHOT builds are ephemeral and overwritable
- For traceability, use commit SHA or CI build number
- Production releases use Git tags with explicit versions

---

## 🔄 Release Workflow

### **Step 1: Development (Feature Branch)**
```
Branch: feat/new-feature
Version: 4.0.0-SNAPSHOT (fixed)
Artifacts: thecore-api:4.0.0-SNAPSHOT
```

### **Step 2: QA Testing (QA Branch)**
```
Branch: qa/new-feature-test
Version: new-feature-test-QA-abc1234
Artifacts: thecore-api:new-feature-test-QA-abc1234
```

### **Step 3: Release Candidate (Release Branch)**
```
Branch: release-4.0.1
Version: 4.0.1-RC-def5678
Artifacts: thecore-api:4.0.1-RC-def5678

Testing: Full integration, performance, security testing
Duration: 1-2 weeks
```

### **Step 4: Production Release (Git Tag)**
```
Tag: v4.0.1 (or 4.0.1)
Branch: main (after merging release-4.0.1)
Version: 4.0.1
Artifacts: thecore-api:4.0.1

Action: Tag the commit, pipeline builds final artifacts
```

### **Step 5: Next Development Cycle**
```
Branch: main (after v4.0.1 tag)
Update: NO CHANGE - remains 4.0.0-SNAPSHOT
Artifacts: thecore-api:4.0.0-SNAPSHOT

Note: SNAPSHOT version NEVER changes, avoiding maintenance burden
```

---

## 🏗️ Implementation Details

### **GitLab CI/CD Configuration**

The VERSION is constructed dynamically in `.gitlab-ci.yml`:

```bash
# Publish and Docker build jobs use this logic:

if [ -n "$CI_COMMIT_TAG" ]; then
  # Production release: Use tag as-is, NO SHA
  export VERSION="${CI_COMMIT_TAG}"

elif echo "$CI_COMMIT_REF_NAME" | grep -q "^release-"; then
  # Release candidate: Include RC + SHA
  BRANCH_NAME=${CI_COMMIT_REF_NAME#release-}
  export VERSION="${BRANCH_NAME}-RC-${CI_COMMIT_SHA:0:7}"

elif echo "$CI_COMMIT_REF_NAME" | grep -q "^qa/"; then
  # QA testing: Include QA + SHA
  BRANCH_NAME=${CI_COMMIT_REF_NAME#qa/}
  export VERSION="${BRANCH_NAME}-QA-${CI_COMMIT_SHA:0:7}"

else
  # Default: SNAPSHOT for other branches (fixed, never changes)
  export VERSION="4.0.0-SNAPSHOT"
fi
```

### **Gradle Configuration**

The version is overridable via `-Pversion` parameter:

```gradle
// build.gradle
allprojects {
    group = 'com.korwe'
    version = project.hasProperty('version') ? project.property('version') : '4.0.0-SNAPSHOT'
}
```

### **Pipeline Stages**

All stages use the same VERSION:

1. **Compile** - Uses SNAPSHOT or dynamic version
2. **Test** - Uses SNAPSHOT or dynamic version
3. **Build** - Creates artifacts with computed version
4. **Publish** - Publishes to Artifactory with computed version
5. **Package** - Builds Docker image with computed version tag

---

## 📊 Version Examples by Branch Type

| Branch | CI_COMMIT_REF_NAME | CI_COMMIT_TAG | VERSION Output |
|--------|-------------------|---------------|----------------|
| `main` | `main` | (empty) | `4.0.0-SNAPSHOT` |
| `feat/api-update` | `feat/api-update` | (empty) | `4.0.0-SNAPSHOT` |
| `qa/full-test` | `qa/full-test` | (empty) | `full-test-QA-abc1234` |
| `release-4.0.1` | `release-4.0.1` | (empty) | `4.0.1-RC-def5678` |
| (tagged commit) | `v4.0.1` | `v4.0.1` | `v4.0.1` |
| (tagged commit) | `4.0.1` | `4.0.1` | `4.0.1` |

---

## ✅ Best Practices

### **DO:**
- ✅ Use Git tags for production releases only
- ✅ Test thoroughly on release-* branches before tagging
- ✅ Keep SNAPSHOT version at 4.0.0-SNAPSHOT (never increment)
- ✅ Use qa/* branches for feature testing before release branches
- ✅ Keep release branches for at least 6 months for audit trail

### **DON'T:**
- ❌ Tag commits without thorough RC testing
- ❌ Deploy RC or QA versions to production
- ❌ Reuse version numbers (each version must be unique)
- ❌ Manually edit version in build.gradle (use CI/CD pipeline)
- ❌ Skip RC stage and tag directly from feature branches

---

## 🔍 Version Verification

### **Check Current Version**
```bash
# In Gradle
./gradlew properties | grep version

# In Git
git describe --tags --always
```

### **Verify Published Artifacts**
```bash
# Maven
curl https://artifactory.paas.bayportfinance.com/artifactory/api/search/artifact?name=thecore-api

# Docker
curl https://docker-registry-default.paas.bayportfinance.com/v2/novalend/thecore-router/tags/list
```

---

## 📝 Creating New Releases

### **For Next Release (e.g., 4.0.1, 4.1.0, 5.0.0)**

**NO VERSION UPDATES NEEDED in code!**

SNAPSHOT version remains at `4.0.0-SNAPSHOT` permanently.

**Steps:**
1. Create release branch: `release-4.0.1`
   - Pipeline auto-generates: `4.0.1-RC-abc1234`
2. Test thoroughly on release branch
3. When ready, tag the commit: `git tag v4.0.1`
   - Pipeline auto-generates: `v4.0.1` (NO SHA)
4. Push tag: `git push origin v4.0.1`
5. Done! Continue development with `4.0.0-SNAPSHOT`

**No file edits required!** Everything is automatic via CI/CD.

### **For Major/Minor Releases**

Follow semantic versioning:
- **MAJOR** (5.0.0): Breaking API changes
- **MINOR** (4.1.0): New features, backward compatible
- **PATCH** (4.0.1): Bug fixes, backward compatible

---

## 🎓 Semantic Versioning Reference

Format: `MAJOR.MINOR.PATCH`

- **MAJOR**: Incompatible API changes (breaking changes)
- **MINOR**: New functionality, backward compatible
- **PATCH**: Bug fixes, backward compatible

Examples:
- `4.0.0` → `4.0.1`: Bug fix release
- `4.0.1` → `4.1.0`: New features added (backward compatible)
- `4.1.0` → `5.0.0`: Breaking API changes

See: https://semver.org/

---

## 📞 Support

**Questions about versioning?**
- Check this document first
- Review `.gitlab-ci.yml` for implementation
- Contact: Platform Team

**Related Documentation:**
- `BUILD-RELEASE-STRATEGY.md` - Overall release strategy
- `GITLAB-CI-FIXES.md` - CI/CD configuration details
- `RELEASE-4.0.0-RC.md` - Release candidate documentation

---

**Last Updated:** 2025-10-28
**Applies To:** the-core-java 4.0.0+
