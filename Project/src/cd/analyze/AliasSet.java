package cd.analyze;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cd.ir.Symbol.MethodSymbol;
import cd.ir.Symbol.TypeSymbol;

public class AliasSet {
	static class AliasSetData {
		private boolean escapes = false;
		//private Set<MethodSymbol> lockedBy = new HashSet<>();
		private final Map<String, AliasSet> fieldMap = new HashMap<>();
		private final List<AliasSet> owners = new ArrayList<>();

		private AliasSetData(AliasSet aliasSet) {
			owners.add(aliasSet);
		}
	}
	
	private AliasSet.AliasSetData ref = new AliasSetData(this);
	
	public static AliasSet BOTTOM = new AliasSet();
	static { BOTTOM.ref = null;	}

	public static AliasSet forType(TypeSymbol type) {
		if (type.isReferenceType()) {
			return new AliasSet();
		}
		return BOTTOM;
	}
	
	/**
	 * Changes ref in all owners of the data
	 */
	private void setRef(AliasSetData newRef) {
		for (AliasSet oldRefOwner : this.ref.owners) {
			oldRefOwner.ref = newRef;
			newRef.owners.add(oldRefOwner);
		}
	}

	public void unify(AliasSet other) {
		if (this.ref == other.ref) return;

		Map<String, AliasSet> thisFields = this.ref.fieldMap;
		Map<String, AliasSet> otherFields = other.ref.fieldMap;

		//this.ref.lockedBy.addAll(other.ref.lockedBy);
		this.ref.escapes |= other.ref.escapes;
		other.setRef(this.ref);

		Set<String> fieldUnion = new HashSet<>(thisFields.keySet());
		fieldUnion.addAll(otherFields.keySet());

		for (String field : fieldUnion) {
			AliasSet thisSet = thisFields.get(field);
			AliasSet otherSet = otherFields.get(field);
			if (thisSet != null && otherSet != null) {
				// field in both maps, unify them
				thisSet.ref.escapes |= this.ref.escapes;
				thisSet.unify(otherSet);
			} else if (thisSet == null) {
				// missing in this
				otherSet.ref.escapes |= this.ref.escapes;
				thisFields.put(field, otherSet);
			}
			// we don't care if otherSet is null, `other` will be deleted
		}
		// `this` is the unified alias set
	}
	
	/*public void unifyEscapes(AliasSet other) {
		if (this.ref == other.ref || ref.escapes == other.ref.escapes) return;
		this.ref.escapes |= other.ref.escapes;

		Map<String, AliasSet> thisFields = this.ref.fieldMap;
		Map<String, AliasSet> otherFields = other.ref.fieldMap;

		Set<String> intersection = new HashSet<>(thisFields.keySet());
		intersection.retainAll(otherFields.keySet());

		for (String field : intersection) {
			thisFields.get(field).unifyEscapes(otherFields.get(field));
		}
	}*/
	
	public AliasSet deepCopy(HashMap<AliasSetData, AliasSet> copies) {
		if (this.isBottom()) return BOTTOM;
		// TODO .equals for aliassetdata messes things up
		AliasSet copy = copies.get(this.ref);
		if (copy == null) {
			copy = new AliasSet();
			copies.put(this.ref, copy);
		} else {
			return copy;
		}

		copy.ref.escapes = this.ref.escapes;
		//copy.ref.lockedBy = new HashSet<>(this.ref.lockedBy);
		for (Entry<String, AliasSet> entry : ref.fieldMap.entrySet()) {
			copy.ref.fieldMap.put(entry.getKey(), entry.getValue().deepCopy(copies));
		}
		return copy;
	}
	
	public boolean isBottom() {
		return this.ref == null;
	}

	public AliasSet fieldMap(String key) {
		AliasSet field = this.ref.fieldMap.get(key);
		if (field == null) {
			field = new AliasSet();
			this.ref.fieldMap.put(key, field);
		}
		return field;
	}
	
	public boolean escapes() {
		return this.ref == null ? false : this.ref.escapes;
	}
	
	public void setEscapes(boolean value) {
		setEscapeRecursive(value, new HashSet<AliasSet>());
	}
	
	/*public Set<MethodSymbol> lockedBy() {
		if (isBottom()) {
			return Collections.<MethodSymbol>emptySet();
		} else {
			return this.ref.lockedBy;
		}
	}
	
	public void addLockedBy(MethodSymbol method) {
		if (!this.isBottom()) {
			this.ref.lockedBy.add(method);
		}
	}*/
	
	private void setEscapeRecursive(boolean value, Set<AliasSet> marked) {
		this.ref.escapes = value;
		marked.add(this);
		for (AliasSet child : this.ref.fieldMap.values()) {
			if (!marked.contains(child)) {
				child.setEscapeRecursive(value, marked);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ref == null) ? 0 : ref.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		// do actual comparison
		AliasSet other = (AliasSet) obj;
		if (ref == null) {
			if (other.ref != null) {
				return false;
			}
		} else if (ref.escapes != other.ref.escapes) {
			return false;
		} else if (ref.fieldMap == null) {
			if (other.ref.fieldMap != null) {
				return false;
			}
		} else if (!ref.fieldMap.equals(other.ref.fieldMap)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringWriter out = new StringWriter();
		toStringRecursive(new PrintWriter(out), new HashSet<AliasSet>());
		return out.toString();
	}
	
	private void toStringRecursive(PrintWriter out, Set<AliasSet> marked) {
		marked.add(this);
		if (isBottom()) {
			out.print("⊥");
		} else {
			out.print("<");
			if (ref.escapes) out.print("Esc,");
			// print lockedBy set
			/*out.print("(");
			Iterator<MethodSymbol> setIter = ref.lockedBy.iterator();
			while (setIter.hasNext()) {
				out.print(setIter.next().fullName());
				if (setIter.hasNext()) {
					out.print(", ");
				}
			}
			out.print("),");*/
			// print fieldMap
			out.print("{");
			Iterator<Entry<String, AliasSet>> mapIter = ref.fieldMap.entrySet().iterator();
			while (mapIter.hasNext()) {
				Entry<String, AliasSet> entry = mapIter.next();
				out.format("%s:", entry.getKey());
				AliasSet child = entry.getValue();
				if (!marked.contains(child)) {
					child.toStringRecursive(out, marked);
				} else {
					out.print("<..>");
				}

				if (mapIter.hasNext()) {
					out.print(", ");
				}
			}
			out.print("}>");
		}
	}
}