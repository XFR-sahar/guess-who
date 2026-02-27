package com.example.fixed_guess_who;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class QuestionCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_QUESTION = 1;

    private final List<QuestionGroup> groups;

    private final List<Object> flatItems = new ArrayList<>();

    private final QuestionClickListener listener;
    public interface QuestionClickListener {
        void onQuestionClicked(Question question);
    }

    // מחלקה שמייצגת קטגוריה אחת
    public static class QuestionGroup {
        String title;
        List<Question> questions;
        boolean isExpanded;

        public QuestionGroup(String title, List<Question> questions, boolean isExpanded) {
            this.title = title;
            this.questions = questions;
            this.isExpanded = isExpanded;
        }
    }

    public QuestionCategoryAdapter(List<QuestionGroup> groups, QuestionClickListener listener) {
        this.groups = groups;
        this.listener = listener;
        prepareFlatList();
    }

    private void prepareFlatList() {
        flatItems.clear();
        for (QuestionGroup group : groups) {
            flatItems.add(group.title);
            if (group.isExpanded) {
                flatItems.addAll(group.questions);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = flatItems.get(position);
        if (item instanceof String) {
            return TYPE_CATEGORY;
        } else {
            return TYPE_QUESTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_CATEGORY) {
            View v = inflater.inflate(R.layout.item_question_category, parent, false);
            return new CategoryVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_question, parent, false);
            return new QuestionVH(v);
        }
    }

    // חיבור הנתונים ל־View – נקרא כל פעם ש-Recyclerview צריך להציג פריט חדש
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CATEGORY) {
            String title = (String) flatItems.get(position);
            // המרה של ה-ViewHolder הכללי ל-CategoryVH ספציפי שמחזיק את ה-TextView של הקטגוריה
            CategoryVH vh = (CategoryVH) holder;
            vh.tvTitle.setText(title);
            // הגדרת מה קורה כשמישהו לוחץ על הכותרת של הקטגוריה
            vh.itemView.setOnClickListener(v -> {

                for (int i = 0; i < groups.size(); i++) {
                    QuestionGroup g = groups.get(i);
                    if (g.title.equals(title)) {
                        g.isExpanded = !g.isExpanded;
                        break;
                    }
                }
                prepareFlatList();
                // עדכון ה-RecyclerView כדי להציג את השינויים
                notifyDataSetChanged();
            });

        } else {
            Question q = (Question) flatItems.get(position);
            // המרה של ה-ViewHolder הכללי ל-QuestionVH ספציפי שמחזיק את TextView של השאלה
            QuestionVH vh = (QuestionVH) holder;
            vh.tvText.setText("• " + q.text);
            vh.itemView.setOnClickListener(v -> {
                // אם ה-listener מוגדר, קריאה לפונקציה שלו עם השאלה שנלחצה
                if (listener != null) {
                    listener.onQuestionClicked(q);
                }
            });
        }
    }



    @Override
    public int getItemCount() {
        return flatItems.size();
    }

    // ViewHolder של קטגוריה
    // מחזיק את TextView של הכותרת
    static class CategoryVH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        CategoryVH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvCategoryTitle);
        }
    }

    // ViewHolder של שאלה
    // מחזיק את TextView של טקסט השאלה
    static class QuestionVH extends RecyclerView.ViewHolder {
        TextView tvText;

        QuestionVH(View v) {
            super(v);
            tvText = v.findViewById(R.id.tvQuestion);
        }
    }
}
