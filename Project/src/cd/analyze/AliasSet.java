package cd.analyze;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cd.ir.Symbol.TypeSymbol;

public class AliasSet {
	static class AliasSetData {
		private boolean escapes = false;
		private final Map<String, AliasSet> fieldMap = new HashMap<>();
	}
	
	private AliasSet.AliasSetData ref = new AliasSetData();
	
	public static AliasSet BOTTOM = new AliasSet();
	static { BOTTOM.ref = null;	}

	public static AliasSet forType(TypeSymbol type) {
		if (type.isReferenceType()) {
			return new AliasSet();
		}
		return BOTTOM;
	}

	void unify(AliasSet other) {
		if (this.ref == other.ref) return;

		Map<String, AliasSet> thisFields = this.ref.fieldMap;
		Map<String, AliasSet> otherFields = other.ref.fieldMap;

		this.ref.escapes |= other.ref.escapes;
		other.ref = this.ref;

		Set<String> fieldUnion = new HashSet<>(thisFields.keySet());
		fieldUnion.addAll(otherFields.keySet());

		for (String field : fieldUnion) {
			AliasSet thisSet = thisFields.get(field);
			AliasSet otherSet = otherFields.get(field);
			if (thisSet != null && otherSet != null) {
				// field in both maps, unify them
				//thisSet.setEscapes(this.ref.escapes);
				thisSet.ref.escapes |= this.ref.escapes;
				thisSet.unify(otherSet);
			} else if (thisSet == null) {
				// missing in this
				//otherSet.setEscapes(this.ref.escapes);
				otherSet.ref.escapes |= this.ref.escapes;
				thisFields.put(field, otherSet);
			}
			// we don't care if otherSet is null, `other` will be deleted
		}

		// `this` is the unified alias set
	}
	
	public void unifyEscapes(AliasSet other) {
		if (this.ref == other.ref || ref.escapes == other.ref.escapes) return;
		this.ref.escapes |= other.ref.escapes;

		Map<String, AliasSet> thisFields = this.ref.fieldMap;
		Map<String, AliasSet> otherFields = other.ref.fieldMap;

		Set<String> intersection = new HashSet<>(thisFields.keySet());
		intersection.retainAll(otherFields.keySet());

		for (String field : intersection) {
			thisFields.get(field).unifyEscapes(otherFields.get(field));
		}
	}
	
	public AliasSet deepCopy() {
		if (this.isBottom()) return BOTTOM;
		
		AliasSet copy = new AliasSet();
		copy.ref.escapes = this.ref.escapes;
		for (Entry<String, AliasSet> entry : ref.fieldMap.entrySet()) {
			AliasSet sub = entry.getValue();
			if (sub == this) {
				sub = copy;
			} else {
				sub = sub.deepCopy();
			}
				// TODO this is probably not enough to deal with selfcontaining sets
			copy.ref.fieldMap.put(entry.getKey(), sub);
		}
		return copy;
	}
	
	public boolean isBottom() {
		return this.ref == null;
	}

	public AliasSet fieldMap(String key) {
		if (!this.ref.fieldMap.containsKey(key)) {
			this.ref.fieldMap.put(key, new AliasSet());
		}
		return this.ref.fieldMap.get(key);
	}
	
	public boolean escapes() {
		return this.ref == null ? false : this.ref.escapes;
	}
	
	public void setEscapes(boolean value) {
		setEscapeRecursive(value, new HashSet<AliasSet>());
	}
	
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
			out.format("<%s, {", ref.escapes);
			Iterator<Entry<String, AliasSet>> iter = ref.fieldMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, AliasSet> entry = iter.next();
				out.format("%s:", entry.getKey());
				AliasSet child = entry.getValue();
				if (!marked.contains(child)) {
					child.toStringRecursive(out, marked);
				} else {
					out.print("<..>");
				}

				if (iter.hasNext()) {
					out.print(", ");
				}
			}
			out.print("}>");
		}
	}
}